package org.wcb;

import org.wcb.action.EmailClient;
import org.wcb.entity.Email;
import org.wcb.entity.EmailServer;
import org.wcb.entity.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Main {
    private static EmailClient client = new EmailClient();
    private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    private static final int smtpPort = 25;
    private static final int pop3Port = 110;
    private static final String smtpPrefix = "smtp.";
    private static final String pop3Prefix = "pop3.";
    private static final int twoSec = 2000;


    public static void main(String[] args) throws IOException, InterruptedException {
        EmailServer server = new EmailServer();
        User user = new User();
        String hostname = "";
        int select = 0;
        // 选择邮件服务器
        do {
            System.out.println("Select your email server: \n" +
                    "1. 163.com\n" +
                    "2. sina.com\n" +
                    "0. exit");
            select = Integer.parseInt(in.readLine());
            switch (select) {
                case 1:
                    hostname = "163.com";
                    break;
                case 2:
                    hostname = "sina.com";
                    break;
                case 0:
                    System.out.println("Bye!");
                    System.exit(0);
                default:
                    System.out.println("Please choose given number!");
                    Thread.sleep(twoSec);
                    break;
            }
        } while ("".equals(hostname));
        // 登录验证
        do {
            System.out.println("Enter your mail address:");
            String email = in.readLine();
            user.setUserEmail(email);
            System.out.println("Enter your password: ");
            String pass = in.readLine();
            user.setPassword(pass);
            server.setHostname(pop3Prefix + hostname);
            server.setPort(pop3Port);
        } while (!client.auth(server, user));
        // 选择功能
        while (true) {
            System.out.println("What do you want?\n" +
                    "1. send Email\n" +
                    "2. Inbox\n" +
                    "0. exit");
            select = Integer.parseInt(in.readLine());
            switch (select) {
                case 1:
                    server.setHostname(smtpPrefix + hostname);
                    server.setPort(smtpPort);
                    sendEmail(server, user);
                    break;
                case 2:
                    server.setHostname(pop3Prefix + hostname);
                    server.setPort(pop3Port);
                    inbox(server, user);
                    break;
                case 0:
                    System.out.println("Bye!");
                    System.exit(0);
                default:
                    System.out.println("Please choose given number!");
                    break;
            }
        }

//        EmailClient emailClient = new EmailClient();
//
//        EmailServer server = new EmailServer("smtp.sina.com", 25);
//        User user = new User("vicriss@sina.com", "vic2sina");
//        Email email = new Email(user.getUserEmail(), "vicriss@163.com", "testMail", "just is a test!");
//        emailClient.sendEmail(server, user, email);

//        EmailServer server = new EmailServer("pop3.sina.com", 110);
//        User user = new User("vicriss@sina.com", "vic2sina");
//        List<Email> emais = emailClient.retrieveEmail(server, user);
//        for (Email email : emais)
//            System.out.println(email);

    }

    public static void sendEmail(EmailServer server, User user) throws IOException {
        Email email = new Email();
        email.setSender(user.getUserEmail());
        System.out.println("Enter the destination: ");
        email.setReceiver(in.readLine());
        System.out.println("Subject: ");
        email.setSubject(in.readLine());
        System.out.println("Content: (end with a new line contain \"$$\")");
        StringBuilder sb = new StringBuilder();
        while (true) {
            String line = in.readLine();
            if ("$$".equals(line)){
                break;
            }
            else {
                sb.append("\r\n");
                sb.append(line);
            }
        }
        email.setBody(sb.toString());
        if (client.sendEmail(server, user, email))
            System.out.println("send a mail successful!");
        else
            System.out.println("send failed!");
    }

    public static void inbox(EmailServer server, User user) throws IOException, InterruptedException {
        List<Email> emails = client.retrieveEmail(server, user);
        inboxLoop: while (true) {
            int num = 0;
            System.out.println("choose an email: ");
            for (int i = 1; i <= emails.size(); i++) {
                System.out.println(i + ". " + emails.get(i-1).getSubject());
            }
            System.out.println();
            System.out.println("-1. delete email");
            System.out.println("0. exit");
            num = Integer.parseInt(in.readLine());
            switch (num) {
                case 0:
                    break inboxLoop;
                case -1:
                    System.out.println("Which email do you want to delete?");
                    num = Integer.parseInt(in.readLine());
                    if (client.deleteEmail(server, user, num)) {
                        emails.remove(num-1);
                        System.out.println("deleted no." + num + " email");
                    } else
                        System.out.println("delete failed!");
                    Thread.sleep(twoSec);
                    break;
                default:
                    if (num < -1 || num > emails.size()) {
                        System.out.println("Please choose given number!");
                        break;
                    } else
                        System.out.println(emails.get(num-1).toString());
                    Thread.sleep(twoSec);
                    break;
            }
        }
    }
}
