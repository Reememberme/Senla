import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Card {
    private String cardNumber;
    private int pin;
    private int balance;
    private boolean isBlocked;
    private int failedAttempts;

    public Card(String cardNumber, int pin, int balance) {
        this.cardNumber = cardNumber;
        this.pin = pin;
        this.balance = balance;
        this.isBlocked = false;
        this.failedAttempts = 0;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public int getPin() {
        return pin;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void blockCard() {
        this.isBlocked = true;
    }

    public void unblockCard() {
        this.isBlocked = false;
        this.failedAttempts = 0;
    }

    public void incrementFailedAttempts() {
        this.failedAttempts++;
        if (this.failedAttempts >= 3) {
            blockCard();
        }
    }
}

class CardDatabase {
    Map<String, Card> cardData = new HashMap<>();
    String filePath = "Data.txt";

    public CardDatabase() {
        loadCardData();
    }

    private void loadCardData() {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 3) {
                    String cardNumber = parts[0];
                    int pin = Integer.parseInt(parts[1]);
                    int balance = Integer.parseInt(parts[2]);
                    cardData.put(cardNumber, new Card(cardNumber, pin, balance));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCardData() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (Card card : cardData.values()) {
                bw.write(card.getCardNumber() + " " + card.getPin() + " " + card.getBalance());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Card getCard(String cardNumber) {
        return cardData.get(cardNumber);
    }
}

class ATM {
    CardDatabase cardDatabase;
    Scanner scanner = new Scanner(System.in);

    public ATM(CardDatabase cardDatabase) {
        this.cardDatabase = cardDatabase;
    }

    public void start() {
        while (true) {
            System.out.print("Введите номер карты: ");
            String cardNumber = scanner.nextLine();
            if (!validateCardNumber(cardNumber)) {
                System.out.print("Неверный формат номера карты.");
                continue;
            }
            Card card = cardDatabase.getCard(cardNumber);
            if (card == null) {
                System.out.println("Карта не найдена.");
                continue;
            }

            if (card.isBlocked()) {
                System.out.println("Карта заблокирована.");
                continue;
            }

            System.out.println("Введите ПИН-код:");
            int pin = scanner.nextInt();
            scanner.nextLine();
            if (card.getPin() != pin) {
                System.out.println("Неверный ПИН-код.");
                card.incrementFailedAttempts();
                cardDatabase.saveCardData();
                continue;
            }

            card.unblockCard();
            cardDatabase.saveCardData();
            showMenu(card);
        }
    }

    private boolean validateCardNumber(String cardNumber) {
        String regex = "^[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(cardNumber);
        return matcher.matches();
    }

    private void showMenu(Card card) {
        while (true) {
            System.out.println("1. Проверить баланс");
            System.out.println("2. Снять средства");
            System.out.println("3. Пополнить баланс");
            System.out.println("4. Выйти");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("Баланс: " + card.getBalance());
                    break;
                case 2:
                    withdraw(card);
                    break;
                case 3:
                    deposit(card);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Неверный выбор.");
            }
        }
    }

    private void withdraw(Card card) {
        System.out.println("Введите сумму для снятия:");
        int amount = scanner.nextInt();
        scanner.nextLine();

        if (amount > card.getBalance()) {
            System.out.println("Недостаточно средств.");
        } else {
            card.setBalance(card.getBalance() - amount);
            cardDatabase.saveCardData();
            System.out.println("Средства сняты. Новый баланс: " + card.getBalance());
        }
    }

    private void deposit(Card card) {
        System.out.println("Введите сумму для пополнения:");
        int amount = scanner.nextInt();
        scanner.nextLine();

        if (amount > 1000000) {
            System.out.println("Сумма пополнения не должна превышать 1 000 000.");
        } else {
            card.setBalance(card.getBalance() + amount);
            cardDatabase.saveCardData();
            System.out.println("Баланс пополнен. Новый баланс: " + card.getBalance());
        }
    }
}

public class Main {
    public static void main(String[] args) {
        CardDatabase cardDatabase = new CardDatabase();
        ATM atm = new ATM(cardDatabase);
        atm.start();
    }
}
//Не смог реализовать блокировку карты на сутки