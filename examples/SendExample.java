import com.anypost.Anypost;
import com.anypost.AnypostException;
import com.anypost.ErrorType;
import com.anypost.model.SendEmailRequest;
import com.anypost.model.SendResponse;

/**
 * Sends a single email.
 *
 * <p>Compile against the built jar and run with an API key in the environment:
 *
 * <pre>{@code
 * mvn -q -DskipTests package
 * javac -cp target/anypost-java-0.1.0.jar examples/SendExample.java -d examples
 * ANYPOST_API_KEY=ap_... java -cp "target/anypost-java-0.1.0.jar:examples" SendExample
 * }</pre>
 */
public final class SendExample {

    public static void main(String[] args) {
        // Reads ANYPOST_API_KEY from the environment. Pass the key explicitly with
        // Anypost.create("ap_...") if you prefer.
        Anypost client = Anypost.fromEnv();

        try {
            SendResponse sent = client.email.send(SendEmailRequest.builder()
                    .from("Acme <you@yourdomain.com>")
                    .to("someone@example.com")
                    .subject("Hello from Anypost")
                    .html("<p>It worked.</p>")
                    .build());
            System.out.println("Queued " + sent.id());
        } catch (AnypostException e) {
            if (e.type() == ErrorType.VALIDATION) {
                System.err.println("validation failed: " + e.validationErrors());
            } else {
                System.err.println("send failed: " + e.getMessage());
            }
            System.exit(1);
        }
    }
}
