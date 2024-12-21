import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

class Block{
    private final int index;
    private final String previousHash;
    private final String data;
    private final String  timestamp;
    private String hash;
    private int nonce;

    public Block(int index , String previousHash , String data){
        this.index = index;
        this.previousHash = previousHash;
        this.data = data;
        this.timestamp = getCurrentTimeStamp();
        this.hash = calculateHash();
        this.nonce = 0;

    }

    public String calculateHash() {
        String input = index + previousHash + timestamp + data + nonce;
        return applySHA256(input);
    }

    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        int loopCount = 0;
        long startTime = System.nanoTime();
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
            loopCount++;

            if (loopCount % 100_000 == 0) {
                double cpuLoad = osBean.getProcessCpuLoad() * 100; // เปลี่ยนเป็นเปอร์เซ็นต์
                long freeMemory = Runtime.getRuntime().freeMemory() / (1024 * 1024); // MB
                System.out.printf("Iteration: %d | CPU Load: %.2f%% | Free Memory: %d MB%n", loopCount, cpuLoad, freeMemory);
            }
        }

        long endTime = System.nanoTime();

        long duration = (endTime - startTime) / 1_000_000;

        System.out.println("Time : " + duration + " milliseconds");
        System.out.println("Block mined! Hash: " + hash);
        System.out.println("Number of iterations: " + loopCount);
    }

    public static String applySHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getCurrentTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    @Override
    public String toString() {
        return "=====================================================================================\n"+
                "Block Details:\n" +
                "Index         : " + index + "\n" +
                "Previous Hash : " + previousHash + "\n" +
                "Data          : " + data + "\n" +
                "Timestamp     : " + timestamp + "\n" +
                "Nonce         : " + nonce + "\n" +
                "Hash          : " + hash;
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }
}

class Blockchain {
    private final ArrayList<Block> chain;
    private final int difficulty = 4;

    public Blockchain(String firstData) {
        chain = new ArrayList<>();
        chain.add(createGenesisBlock(firstData));
    }

    private Block createGenesisBlock(String firstData) {
        Block genesisBlock = new Block(0, "0", firstData);
        genesisBlock.mineBlock(difficulty);
        return genesisBlock;
    }

    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public void addBlock(String data) {
        Block latestBlock = getLatestBlock();
        Block newBlock = new Block(chain.size(), latestBlock.getHash(), data);
        newBlock.mineBlock(difficulty);
        chain.add(newBlock);
    }

    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            // ตรวจสอบว่าแฮชปัจจุบันถูกต้อง
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                return false;
            }

            // ตรวจสอบว่าแฮชก่อนหน้าในบล็อกปัจจุบันตรงกับแฮชของบล็อกก่อนหน้า
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                return false;
            }
        }
        return true;
    }

    public void printBlockchain() {
        for (Block block : chain) {
            System.out.println(block.toString());
        }
        if (isChainValid()) {
            System.out.println("\nThe blockchain is valid");
        } else {
            System.out.println("\nThe blockchain is invalid");
        }
    }
}


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter data for the Genesis Block:");
        String firstData = scanner.nextLine();
        Blockchain blockchain = new Blockchain(firstData);

        // เริ่มต้นลูปหลักในการเพิ่มบล็อก
        while (true) {
            System.out.print("Enter data for the block (or type 'exit' to stop): ");
            String data = scanner.nextLine();

            // เงื่อนไขในการออกจากโปรแกรม
            if (data.equalsIgnoreCase("exit")) {
                System.out.println("Exiting the blockchain...");
                break;
            }

            // เพิ่มบล็อกใหม่ในบล็อกเชน
            blockchain.addBlock(data);


        }

        // แสดงบล็อกเชนที่อัปเดต
        System.out.println("\nCurrent Blockchain:");
        blockchain.printBlockchain();



        scanner.close();
    }

}