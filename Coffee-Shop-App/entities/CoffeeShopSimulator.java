import java.util.Scanner;

public class CoffeeShopSimulator {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to Coffee Shop!");
        System.out.print("Choose coffee (Espresso/Cappuccino): ");
        String coffeeType = scanner.nextLine();

        System.out.print("Choose size (Small/Medium/Large): ");
        CoffeeSize size = CoffeeSize.valueOf(scanner.nextLine().toUpperCase());

        Coffee coffee = CoffeeFactory.createCoffee(coffeeType, size);

        System.out.println("Total Cost: $" + coffee.getCost());

        System.out.print("Choose payment method (CreditCard/PayPal): ");
        String paymentType = scanner.nextLine().toLowerCase();
        PaymentMethod paymentMethod = switch (paymentType) {
            case "creditcard" -> new CreditCardPayment("4111111111111111", "12/25", "123");
            case "paypal" -> new PayPalPayment();
            default -> throw new IllegalArgumentException("Unknown payment method");
        };

        paymentMethod.processPayment(coffee.getCost());

        System.out.println("Thank yEou for your purchase!");
    }
}

