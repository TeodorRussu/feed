package news.feed.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;

@Component
public class EmailSender {
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private Environment env;

    public void sendEmail() {

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo("teodor.russu@elm327.ro", "teodor_russu@yahoo.com");

        msg.setSubject("Testing from Spring Boot");
        msg.setText("Hello World \n Spring Boot Email");

        javaMailSender.send(msg);

    }

    public void sendEmailWithAttachment(String to, String title, String content, String filename, String path) throws MessagingException, IOException {

        MimeMessage msg = javaMailSender.createMimeMessage();

        // true = multipart message
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);

        helper.setTo(to);

        helper.setSubject(title);

        // default = text/plain
        //helper.setText("Check attachment for image!");

        // true = text/html
        helper.setText(content, true);

        // hard coded a file path
        FileSystemResource file = new FileSystemResource(new File(path));

        helper.addAttachment(filename, file);

        javaMailSender.send(msg);

    }
}
