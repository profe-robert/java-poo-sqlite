package app.sqlite;

import app.sqlite.view.ProductoView;

public class AppSqlite {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> { 
            new ProductoView().setVisible(true); 
        });
    }
    
}
