package org.wcb.action;

import org.wcb.entity.Email;
import org.wcb.entity.EmailServer;
import org.wcb.entity.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wybe on 7/1/16.
 */
public class EmailClient {
    private PrintWriter pw;
    private BufferedReader br;
    private Socket socket;

    public boolean sendEmail(EmailServer server, User user, Email email) {
        try {
            connect(server);
            sendCommend("HELO " + user.getUserEmail(),"250", "hello failed!");
            sendCommend("AUTH LOGIN","334", "request failed!");
            sendCommend(Base64.getEncoder().encodeToString(user.getUserEmail().getBytes()),"334", "input something wrong!");
            sendCommend(Base64.getEncoder().encodeToString(user.getPassword().getBytes()),"235", "auth failed, please confirm your mail address or password");
            sendCommend("MAIL FROM:<" + email.getSender() + ">","250", "input something wrong!");
            sendCommend("RCPT TO:<" + email.getReceiver() + ">","250", "input something wrong!");
            sendCommend("DATA","354", "request failed!");
            String data = String.format("From:<%s>\r\nTo:<%s>\r\nSubject:%s\r\n\r\n%s\r\n.",
                    email.getSender(), email.getReceiver(), email.getSubject(), email.getBody());
            sendCommend(data,"250", "input something wrong!");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                sendCommend("QUIT","221", "request failed!");
                closeConnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Email> retrieveEmail(EmailServer server, User user) {
        try {
            auth(server, user);
            int msgCount = getMessageCount();
//            System.out.println("you have " + msgCount + " emails");
            List<Email> emails = new ArrayList<>();
            for (int i = 1; i <= msgCount; i++) {
                emails.add( getEmail(i));
            }
            return emails;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                sendCommend("QUIT", "+OK", "quit failed!");
                closeConnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean deleteEmail(EmailServer server, User user, int messageId) {
        try {
            auth(server, user);
            sendCommend("DELE " + messageId, "+OK", "delete failed!");
            return true;
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                sendCommend("QUIT", "+OK", "quit failed!");
                closeConnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean auth(EmailServer server, User user) {
        try {
            connect(server);
            sendCommend("USER " + user.getUserEmail(), "+OK", "input something wrong!");
            sendCommend("PASS " + user.getPassword(), "+OK", "auth failed, please confirm your mail address or password");
        } catch (IOException e) {
            e.printStackTrace();
            try {
                sendCommend("QUIT", "+OK", "quit failed!");
                closeConnect();
                return false;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 发送命令并获取返回信息
     * @param commend
     * @param check
     * @param exceptionMsg
     * @return
     * @throws IOException
     */
    private String sendCommend(String commend, String check, String exceptionMsg) throws IOException {
        pw.println(commend);
        pw.flush();
        return getRespondAndCheck(check,exceptionMsg);
    }

    /**
     * 接收返回信息并检查是否错误
     * @param check
     * @param exceptionMsg
     * @return
     * @throws IOException
     */
    private String getRespondAndCheck(String check, String exceptionMsg) throws IOException {
        String respond = br.readLine();
        System.out.println(respond);
        if (!respond.startsWith(check))
            throw new IOException(exceptionMsg);
        return respond;
    }

    /**
     * 获取邮件数量
     * @return
     * @throws IOException
     */
    public int getMessageCount() throws IOException {
        String response =sendCommend("STAT", "+OK", "request failed!");

        try {
            String countStr = response.split(" ")[1];
            int count = new Integer(countStr);
            return count;
        } catch (Exception e) {
            throw new IOException("Invalid response - " + response);
        }
    }

    /**
     * 获取邮件
     * @param messageId
     * @return
     * @throws IOException
     */
    public Email getEmail(int messageId) throws IOException {
        Email email = new Email();
        sendCommend("RETR " + messageId, "+OK", "getMessage failed!");
        String[] messageLines = getMultilineResponse();
        email = getHeader(messageLines);
        email.setBody(getBody(messageLines));
        return email;
    }

    /**
     * 获取邮件正文内容
     * @param messageLines
     * @return
     * @throws IOException
     */
    public String getBody(String[] messageLines) throws IOException {
        StringBuilder message = new StringBuilder();
        int blankLine = 0;  //以空行分割头部信息和正文信息
        while (blankLine < messageLines.length && !("".equals(messageLines[blankLine])))
            blankLine++;    //确定空行的位置
        for (int i = blankLine + 1; i < messageLines.length; i++) {
            if (!"".equals(messageLines[i])) {
                message.append(messageLines[i]);
                message.append("\n");
            }
        }
        return message.toString();
    }

    /**
     * 获取邮件基本头部信息
     * @param messageLines
     * @return
     * @throws IOException
     */
    public Email getHeader(String[] messageLines) throws IOException {
        Email emailHeader = new Email();
        for (String line : messageLines) {  //对每一行进行正则匹配
            if (emailHeader.getSender() == null || emailHeader.getSender().isEmpty())
                emailHeader.setSender(regexPattern("from", line));
            if (emailHeader.getReceiver() == null || emailHeader.getReceiver().isEmpty())
                emailHeader.setReceiver(regexPattern("to", line));
            if (emailHeader.getDate() == null || emailHeader.getDate().isEmpty())
                emailHeader.setDate(regexPattern("date", line));
            if (emailHeader.getSubject() == null || emailHeader.getSubject().isEmpty())
                emailHeader.setSubject(regexPattern("subject", line));
        }
        return emailHeader;
    }

    /**
     * 正则匹配，用于获取头部信息
     * @param pattern
     * @param line
     * @return
     */
    private String regexPattern(String pattern, String line) {
        Pattern p;
        switch (pattern) {
            case "from":
                p = Pattern.compile("^[F|f]rom:(.+)");
                break;
            case "to":
                p = Pattern.compile("^[T|t]o:(.+)");
                break;
            case "date":
                p = Pattern.compile("^[D|d]ate:(.+)");
                break;
            case "subject":
                p = Pattern.compile("^[S|s]ubject:(.+)");
                break;
            default:
                p = Pattern.compile("");
        }

        Matcher m = p.matcher(line);
        if (m.find())
            return m.group(1);
        else
            return "";
    }

    /**
     * 获取多行返回信息
     * @return
     * @throws IOException
     */
    public String[] getMultilineResponse() throws IOException {
        ArrayList<String> lines = new ArrayList<>();

        while (true) {
            String line = br.readLine();

            if (line == null) {
                throw new IOException("Server unawares closed the connection.");
            }
            if (line.equals(".")) {
                break;
            }
            if ((line.length() > 0) && (line.charAt(0) == '.')) {
                line = line.substring(1);
            }

            lines.add(line);
        }

        String response[] = new String[lines.size()];
        lines.toArray(response);
        return response;
    }

    /**
     * 发送连接请求
     * @param server
     * @throws IOException
     */
    private void connect(EmailServer server) throws IOException {
        socket= new Socket(server.getHostname(), server.getPort());
        pw = new PrintWriter(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String check;
        switch (server.getHostname().substring(0,4)) {
            case "smtp":
                check = "220";
                break;
            case "pop3":
                check = "+OK";
                break;
            default:
                check = "";
                break;
        }
        getRespondAndCheck(check, "connect failed!");
    }

    /**
     * 关闭连接
     * @throws IOException
     */
    private void closeConnect() throws IOException {
        if (socket != null) {
            socket.shutdownInput();
            socket.shutdownOutput();
        }
        if (pw != null)
            pw.close();
        if (br !=null)
            br.close();
        if (socket != null)
            socket.close();
    }
}
