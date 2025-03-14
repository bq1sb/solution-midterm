import java.util.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Base64;
import java.math.BigDecimal;

class PayPalPayment implements PaymentMethod {
    private static final String CLIENT_ID = "AQ9JqalXQSX2szxMx7sWmvnMjso97LaIBOJkhj0OTBUDJ7F8c8Elakwwn541_Ax3YeBoaOuO0vpbYZZF";
    private static final String CLIENT_SECRET = "EELHeGl8y-b7qY06WRzMRrxQgTDbNo9kmIU9pXR_gfqm-nnIdn5DwykOypOCBsOqrQYSfikLFfWuwmpY";

    public boolean processPayment(double amount) {
        try {
            // 1️⃣ Получить access_token
            String accessToken = getAccessToken();
            if (accessToken == null) {
                System.out.println("Ошибка: Не удалось получить access token.");
                return false;
            }

            // 2️⃣ Создать платеж
            String approvalUrl = createPayment(accessToken, amount);
            if (approvalUrl == null) {
                System.out.println("Ошибка: Не удалось создать платеж.");
                return false;
            }

            // 3️⃣ Попросить пользователя оплатить
            System.out.println("Перейдите по этой ссылке для оплаты: " + approvalUrl);
            System.out.print("После оплаты введите Payer ID из URL: ");
            Scanner scanner = new Scanner(System.in);
            String payerId = scanner.nextLine();

            // 4️⃣ Завершить платеж
            return executePayment(accessToken, payerId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Получить Access Token ---
    private String getAccessToken() throws IOException {
        String auth = CLIENT_ID + ":" + CLIENT_SECRET;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        URL url = new URL("https://api-m.sandbox.paypal.com/v1/oauth2/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Accept-Language", "en_US");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write("grant_type=client_credentials".getBytes());
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            System.out.println("Ошибка получения токена: HTTP " + responseCode);
            return null;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }

        JSONObject jsonResponse = new JSONObject(response.toString());
        return jsonResponse.getString("access_token");
    }

    // --- Создать платеж ---
    private String createPayment(String accessToken, double amount) throws IOException {
        URL url = new URL("https://api-m.sandbox.paypal.com/v1/payments/payment");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        // ✅ Используем BigDecimal для точного представления суммы
        BigDecimal formattedAmount = BigDecimal.valueOf(amount).setScale(2, BigDecimal.ROUND_HALF_UP);

        String jsonInputString = new JSONObject()
                .put("intent", "sale")
                .put("payer", new JSONObject().put("payment_method", "paypal"))
                .put("transactions", new JSONArray()
                        .put(new JSONObject()
                                .put("amount", new JSONObject()
                                        .put("total", formattedAmount.toString())  // ✅ PayPal требует строку
                                        .put("currency", "USD"))
                                .put("description", "Coffee purchase")))
                .put("redirect_urls", new JSONObject()
                        .put("return_url", "https://example.com/success")
                        .put("cancel_url", "https://example.com/cancel"))
                .toString();

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonInputString.getBytes("utf-8"));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 201) {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            System.out.println("Ошибка PayPal: " + errorResponse);
            return null;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }

        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray links = jsonResponse.getJSONArray("links");
        for (int i = 0; i < links.length(); i++) {
            JSONObject link = links.getJSONObject(i);
            if ("approval_url".equals(link.getString("rel"))) {
                return link.getString("href");
            }
        }

        return null;
    }


    // --- Завершить платеж ---
    private boolean executePayment(String accessToken, String payerId) throws IOException {
        // ⚠ Здесь должен быть реальный Payment ID, полученный после создания платежа.
        String paymentId = "ВАШ_PAYMENT_ID"; // TODO: заменить на реальный ID

        URL url = new URL("https://api-m.sandbox.paypal.com/v1/payments/payment/" + paymentId + "/execute");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        String jsonInputString = new JSONObject()
                .put("payer_id", payerId)
                .toString();

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonInputString.getBytes("utf-8"));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            System.out.println("Ошибка выполнения платежа: HTTP " + responseCode);
            return false;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }

        JSONObject jsonResponse = new JSONObject(response.toString());
        return jsonResponse.getString("state").equals("approved");
    }
}