package app.sqlite.controller;

import java.sql.SQLException; 
import java.util.List;

import app.sqlite.dao.ProductoDAO;
import app.sqlite.model.Producto;
 
public class ProductoController { 
 
    private ProductoDAO productoDAO; 
 
    public ProductoController() { 
        this.productoDAO = new ProductoDAO(); 
    }
    
    public void guardarProducto(String nombre, double precio, int stock) throws SQLException { 
        Producto p = new Producto(); 
        p.setNombre(nombre); 
        p.setPrecio(precio); 
        p.setStock(stock); 
        productoDAO.insertar(p); 
    }
    
    public void actualizarProducto(int id, String nombre, double precio, int stock) throws SQLException { 
        Producto p = new Producto(id, nombre, precio, stock); 
        productoDAO.modificar(p); 
    }
    
    public void eliminarProducto(int id) throws SQLException { 
        productoDAO.eliminar(id); 
    }
    
    public Producto buscarProducto(int id) throws SQLException { 
        return productoDAO.buscarPorId(id); 
    }
    
    public List<Producto> listarProductos() throws SQLException { 
        return productoDAO.listar(); 
    } 
} 