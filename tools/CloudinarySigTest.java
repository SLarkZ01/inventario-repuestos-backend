import com.repobackend.api.cloud.service.CloudinaryService;
import java.util.Map;

public class CloudinarySigTest {
    public static void main(String[] args) {
        String cloudName = System.getenv("CLOUDINARY_CLOUD_NAME");
        String apiKey = System.getenv("CLOUDINARY_API_KEY");
        String apiSecret = System.getenv("CLOUDINARY_API_SECRET");
        CloudinaryService svc = new CloudinaryService(cloudName, apiKey, apiSecret);
        Map<String,String> params = Map.of("folder","products/test");
        Map<String,Object> sig = svc.generateSignature(params);
        System.out.println("signature: " + sig.get("signature"));
        System.out.println("timestamp: " + sig.get("timestamp"));
        System.out.println("apiKey: " + sig.get("apiKey"));
    }
}
