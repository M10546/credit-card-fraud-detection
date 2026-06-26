import java.util.*;

public class CreditCardFraudDetectionSystem {

    // Transaction Model
    static class Transaction {
        String transactionId;
        String cardId;
        double amount;
        String merchantCategory;
        String location;
        long timestamp;

        public Transaction(String transactionId, String cardId,
                           double amount, String merchantCategory,
                           String location, long timestamp) {
            this.transactionId = transactionId;
            this.cardId = cardId;
            this.amount = amount;
            this.merchantCategory = merchantCategory;
            this.location = location;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return String.format(
                    "Transaction{id='%s', amount=%.2f, category='%s', location='%s'}",
                    transactionId, amount, merchantCategory, location);
        }
    }

    // Rule-Based Fraud Detection Engine
    static class FraudDetector {

        private final Map<String, List<Transaction>> transactionHistory = new HashMap<>();

        // Thresholds
        private static final double HIGH_AMOUNT_THRESHOLD = 50000;
        private static final int MAX_TRANSACTIONS_PER_HOUR = 5;

        private final Set<String> riskyCategories =
                new HashSet<>(Arrays.asList(
                        "CRYPTO",
                        "GAMBLING",
                        "LUXURY",
                        "INTERNATIONAL_TRANSFER"
                ));

        public boolean isFraud(Transaction transaction) {

            List<String> triggeredRules = new ArrayList<>();

            // Rule 1: High-value transaction
            if (transaction.amount > HIGH_AMOUNT_THRESHOLD) {
                triggeredRules.add("HIGH_AMOUNT");
            }

            // Rule 2: Risky merchant category
            if (riskyCategories.contains(transaction.merchantCategory)) {
                triggeredRules.add("RISKY_MERCHANT");
            }

            // Rule 3: Frequency anomaly
            if (checkFrequency(transaction)) {
                triggeredRules.add("HIGH_FREQUENCY");
            }

            // Rule 4: Geographic anomaly
            if (checkLocationAnomaly(transaction)) {
                triggeredRules.add("LOCATION_CHANGE");
            }

            saveTransaction(transaction);

            if (!triggeredRules.isEmpty()) {
                System.out.println("\n⚠ FRAUD ALERT");
                System.out.println("Transaction: " + transaction);
                System.out.println("Triggered Rules: " + triggeredRules);
                return true;
            }

            return false;
        }

        // Frequency detection
        private boolean checkFrequency(Transaction transaction) {

            List<Transaction> history =
                    transactionHistory.getOrDefault(
                            transaction.cardId,
                            new ArrayList<>());

            long oneHour = 60 * 60 * 1000;

            int count = 0;

            for (Transaction t : history) {
                if (transaction.timestamp - t.timestamp <= oneHour) {
                    count++;
                }
            }

            return count >= MAX_TRANSACTIONS_PER_HOUR;
        }

        // Geographic anomaly detection
        private boolean checkLocationAnomaly(Transaction transaction) {

            List<Transaction> history =
                    transactionHistory.getOrDefault(
                            transaction.cardId,
                            new ArrayList<>());

            if (history.isEmpty()) {
                return false;
            }

            Transaction previous =
                    history.get(history.size() - 1);

            return !previous.location.equals(transaction.location);
        }

        private void saveTransaction(Transaction transaction) {
            transactionHistory
                    .computeIfAbsent(
                            transaction.cardId,
                            k -> new ArrayList<>())
                    .add(transaction);
        }
    }

    // Simplified Decision Tree Classifier
    static class DecisionTreeClassifier {

        public boolean predict(Transaction transaction) {

            if (transaction.amount > 30000) {

                if (transaction.merchantCategory.equals("CRYPTO")
                        || transaction.merchantCategory.equals("GAMBLING")) {
                    return true;
                }

                if (!transaction.location.equals("INDIA")) {
                    return true;
                }
            }

            return false;
        }
    }

    // Evaluation Metrics
    static class Evaluator {

        int TP = 0, TN = 0, FP = 0, FN = 0;

        public void update(boolean actual, boolean predicted) {

            if (actual && predicted) TP++;
            else if (!actual && !predicted) TN++;
            else if (!actual && predicted) FP++;
            else FN++;
        }

        public void printReport() {

            double accuracy =
                    (double)(TP + TN) /
                    (TP + TN + FP + FN);

            double precision =
                    TP + FP == 0 ? 0 :
                    (double) TP / (TP + FP);

            double recall =
                    TP + FN == 0 ? 0 :
                    (double) TP / (TP + FN);

            System.out.println("\n===== Evaluation Report =====");
            System.out.println("Accuracy : " +
                    String.format("%.2f", accuracy * 100) + "%");
            System.out.println("Precision: " +
                    String.format("%.2f", precision * 100) + "%");
            System.out.println("Recall   : " +
                    String.format("%.2f", recall * 100) + "%");
        }
    }

    public static void main(String[] args) {

        FraudDetector ruleEngine =
                new FraudDetector();

        DecisionTreeClassifier classifier =
                new DecisionTreeClassifier();

        Evaluator evaluator =
                new Evaluator();

        List<Transaction> transactions =
                Arrays.asList(

                        new Transaction(
                                "TX001",
                                "CARD001",
                                1500,
                                "FOOD",
                                "INDIA",
                                System.currentTimeMillis()),

                        new Transaction(
                                "TX002",
                                "CARD001",
                                70000,
                                "LUXURY",
                                "INDIA",
                                System.currentTimeMillis()),

                        new Transaction(
                                "TX003",
                                "CARD001",
                                45000,
                                "CRYPTO",
                                "USA",
                                System.currentTimeMillis()),

                        new Transaction(
                                "TX004",
                                "CARD002",
                                800,
                                "GROCERY",
                                "INDIA",
                                System.currentTimeMillis())
                );

        // Ground truth for testing
        boolean[] actualFraud = {
                false,
                true,
                true,
                false
        };

        System.out.println("===== RULE-BASED DETECTION =====");

        for (int i = 0; i < transactions.size(); i++) {

            boolean predicted =
                    ruleEngine.isFraud(transactions.get(i));

            evaluator.update(
                    actualFraud[i],
                    predicted);
        }

        evaluator.printReport();

        System.out.println("\n===== DECISION TREE RESULTS =====");

        for (Transaction t : transactions) {
            System.out.println(
                    t.transactionId +
                    " -> Fraud: " +
                    classifier.predict(t));
        }
    }
}