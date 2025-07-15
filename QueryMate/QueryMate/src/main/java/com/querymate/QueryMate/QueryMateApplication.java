package com.querymate.QueryMate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class QueryMateApplication {

	public static void main(String[] args) {
		// ✅ Load .env variables and inject into System environment
//		Dotenv dotenv = Dotenv.configure()
//				.directory("./")     // Looks for .env in the root (adjust if needed)
//				.ignoreIfMissing()   // Won’t crash if .env is missing (good for prod)
//				.load();
//
//		// ✅ Set all needed environment variables
//		setEnv("DB_HOST", dotenv);
//		setEnv("DB_PORT", dotenv);
//		setEnv("DB_NAME", dotenv);
//		setEnv("DB_USERNAME", dotenv);
//		setEnv("DB_PASSWORD", dotenv);
//		setEnv("JWT_SECRET", dotenv);
//		setEnv("JWT_EXPIRATION", dotenv);
//		setEnv("OPENROUTER_API_KEY", dotenv);
//		setEnv("OPENROUTER_MODEL", dotenv);
//		setEnv("OPENROUTER_REFERER", dotenv);
//		setEnv("APP_CRYPTO_SECRET", dotenv);

		SpringApplication.run(QueryMateApplication.class, args);
	}

//	private static void setEnv(String key, Dotenv dotenv) {
//		String value = dotenv.get(key);
//		if (value != null) {
//			System.setProperty(key, value);
//		}
//	}
}
