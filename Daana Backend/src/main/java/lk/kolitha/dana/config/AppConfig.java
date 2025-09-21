package lk.kolitha.dana.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Properties;

@EnableCaching
@EnableScheduling
@EnableAsync
@Configuration
public class AppConfig {

    private final Environment environment;

    public AppConfig(Environment environment) {
        this.environment = environment;
    }


    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }


    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(environment.getRequiredProperty("daana.aws.mail.host"));
        mailSender.setPort(Integer.parseInt(environment.getRequiredProperty("daana.aws.mail.port")));
        mailSender.setUsername(environment.getRequiredProperty("daana.aws.mail.username"));
        mailSender.setPassword(environment.getRequiredProperty("daana.aws.mail.password"));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", environment.getRequiredProperty("daana.aws.mail.transport.protocol"));
        props.put("mail.smtp.port", environment.getRequiredProperty("daana.aws.mail.port"));
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        return mailSender;
    }


    @Bean
    public AmazonS3 s3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(
                environment.getRequiredProperty("aws.s3.access-key"),
                environment.getRequiredProperty("aws.s3.secret-key")
        );
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(environment.getRequiredProperty("aws.s3.region"))
                .build();
    }

}
