import view.View;

public class Main {
    public static void main(String[] args) {

//        System.out.println("Starting JPA...");

//        // Tạo EntityManager → Hibernate sẽ đọc persistence.xml
//        EntityManager em = MariaDbJPAConnection.getInstance();
//
//        System.out.println("JPA started successfully!");

        View.launch(View.class);
    }
}
