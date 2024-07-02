package org.example.champicker;

import com.merakianalytics.orianna.Orianna;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;

import org.springframework.stereotype.Component;

@SpringBootApplication
@Component
public class ChamPickerApplication {
	private static final String riotAPIKey = "<your API key>";
	public static void main(String[] args) {
		Orianna.loadConfiguration("config.json");
		Orianna.setRiotAPIKey(riotAPIKey);
		SpringApplication.run(ChamPickerApplication.class, args);
	}

}
