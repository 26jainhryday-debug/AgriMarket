import dao.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        FarmerDAO fdao = new FarmerDAO();
        ProductDAO pdao = new ProductDAO();
        UserDAO udao = new UserDAO();

        fdao.createTable();
        pdao.createTable();
        udao.createTable();

        Scanner sc = new Scanner(System.in);

        System.out.print("Username: ");
        String u = sc.next();
        System.out.print("Password: ");
        String p = sc.next();

        String role = udao.login(u, p);

        if (role == null) {
            System.out.println("Invalid login");
            return;
        }

        System.out.println("Login successful: " + role);
    }
}