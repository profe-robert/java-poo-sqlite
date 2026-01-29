package app.sqlite.dao;

import java.sql.*; 
import java.util.ArrayList; 
import java.util.List;

import app.sqlite.model.Producto;
import app.sqlite.util.Conexion;
 
public class ProductoDAO { 
 
    public void insertar(Producto producto) throws SQLException { 
        String sql = "INSERT INTO producto (nombre, precio, stock) VALUES (?, ?, ?)"; 
        try (
            Connection cn = Conexion.getConnection();
            PreparedStatement ps = cn.prepareStatement(sql)
        ){
            ps.setString(1, producto.getNombre()); 
            ps.setDouble(2, producto.getPrecio()); 
            ps.setInt(3, producto.getStock()); 
            ps.executeUpdate(); 
        } 
    } 
 
    public void modificar(Producto producto) throws SQLException { 
        String sql = "UPDATE producto SET nombre = ?, precio = ?, stock = ? WHERE id = ?"; 
        try (
            Connection cn = Conexion.getConnection(); 
            PreparedStatement ps = cn.prepareStatement(sql)
        ){ 
 
            ps.setString(1, producto.getNombre()); 
            ps.setDouble(2, producto.getPrecio()); 
            ps.setInt(3, producto.getStock()); 
            ps.setInt(4, producto.getId()); 
            ps.executeUpdate(); 
        } 
    } 
 
    public void eliminar(int id) throws SQLException { 
        String sql = "DELETE FROM producto WHERE id = ?"; 
        try (Connection cn = Conexion.getConnection(); 
             PreparedStatement ps = cn.prepareStatement(sql)) { 
 
            ps.setInt(1, id); 
            ps.executeUpdate(); 
        } 
    } 
 
    public Producto buscarPorId(int id) throws SQLException { 
        String sql = "SELECT * FROM producto WHERE id = ?"; 
        try (Connection cn = Conexion.getConnection(); 
             PreparedStatement ps = cn.prepareStatement(sql)) { 
 
            ps.setInt(1, id); 
            ResultSet rs = ps.executeQuery(); 
            if (rs.next()) { 
                return new Producto( 
                        rs.getInt("id"), 
                        rs.getString("nombre"), 
                        rs.getDouble("precio"), 
                        rs.getInt("stock") 
                ); 
            } 
            return null; 
        } 
    } 
 
    public List<Producto> listar() throws SQLException { 
        String sql = "SELECT * FROM producto"; 
        List<Producto> lista = new ArrayList<>(); 
 
        try (Connection cn = Conexion.getConnection(); 
             PreparedStatement ps = cn.prepareStatement(sql); 
             ResultSet rs = ps.executeQuery()) { 
 
            while (rs.next()) { 
                Producto p = new Producto( 
                        rs.getInt("id"), 
                        rs.getString("nombre"), 
                        rs.getDouble("precio"), 
                        rs.getInt("stock") 
                ); 
                lista.add(p); 
            } 
        } 
        return lista; 
    } 
}
