package com.example.geekshop.View

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geekshop.R
import com.example.geekshop.data.adapters.CategoryAdapter
import com.example.geekshop.data.adapters.ProductAdapter
import com.example.geekshop.data.model.Category
import com.example.geekshop.data.model.Products
import com.example.geekshop.data.db.SQLite
import com.example.geekshop.repository.MainRepository
import com.example.geekshop.viewmodel.MainViewModel
import com.example.geekshop.viewmodel.MainViewModel.Companion.provideFactory

/**
 * Главная активность приложения, отображающая список товаров и категорий
 */
class MainActivity : AppCompatActivity(), ProductAdapter.ProductActionListener, CategoryAdapter.CategoryActionListener {

    // Адаптеры для RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    // База данных и ViewModel
    private lateinit var db: SQLite
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Инициализация базы данных и ViewModel
        initDatabaseAndViewModel()

        // Настройка RecyclerView для товаров и категорий
        setupRecyclerViews()

        // Проверка авторизации пользователя
        checkAuth()

        // Настройка нижней панели навигации
        setupTabBar()
    }

    /**
     * Инициализация базы данных и ViewModel
     */
    private fun initDatabaseAndViewModel() {
        db = SQLite.getInstance(this)
        val repository = MainRepository(db)
        viewModel = ViewModelProvider(this, provideFactory(repository, this))[MainViewModel::class.java]

        // Подписка на изменения данных
        observeData()
    }

    /**
     * Подписка на LiveData из ViewModel
     */
    private fun observeData() {
        // Обновление списка товаров при изменении данных
        viewModel.products.observe(this) { products ->
            productAdapter.updateProducts(products)
        }

        // Обновление списка категорий при изменении данных
        viewModel.categories.observe(this) { categories ->
            categoryAdapter.updateCategories(categories)
        }
    }

    /**
     * Настройка RecyclerView для товаров и категорий
     */
    private fun setupRecyclerViews() {
        // Настройка RecyclerView для товаров (2 колонки)
        val productsRecyclerView = findViewById<RecyclerView>(R.id.productsView)
        productAdapter = ProductAdapter(listOf(), this)
        productsRecyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = productAdapter
        }

        // Настройка горизонтального RecyclerView для категорий
        val categoriesRecyclerView = findViewById<RecyclerView>(R.id.viewCategory)
        categoryAdapter = CategoryAdapter(listOf(), this)
        categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
    }

    /**
     * Проверка авторизации пользователя
     */
    private fun checkAuth() {
        if (viewModel.getCurrentUserId() == -1) {
            // Если пользователь не авторизован, перенаправляем на экран авторизации
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }

    /**
     * Настройка нижней панели навигации
     */
    private fun setupTabBar() {
        // Кнопка перехода в профиль
        findViewById<ImageView>(R.id.btn_profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Кнопка перехода в корзину
        findViewById<ImageView>(R.id.btn_cart).setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    // Реализация интерфейса ProductActionListener
    override fun onProductClick(product: Products) {
        showProductDialog(product)
    }

    override fun onAddToCartClick(product: Products) {
        val userId = viewModel.getCurrentUserId()
        if (userId == -1) {
            Toast.makeText(this, "Требуется авторизация", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, AuthActivity::class.java))
            return
        }

        viewModel.addProductToCart(product.id)
        Toast.makeText(this, "${product.Name} добавлен в корзину", Toast.LENGTH_SHORT).show()
    }

    // Реализация интерфейса CategoryActionListener
    override fun onCategoryClick(category: Category) {
        viewModel.updateProductsByCategory(category.Title)
    }

    /**
     * Показывает диалог с подробной информацией о товаре
     * @param product Товар для отображения
     */
    private fun showProductDialog(product: Products) {
        Dialog(this).apply {
            setContentView(R.layout.style_full_product)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // Заполнение данных о товаре
            findViewById<ImageView>(R.id.dialogImageProduct).setImageResource(product.imageId)
            findViewById<TextView>(R.id.dialogTitleProduct).text = product.Name
            findViewById<TextView>(R.id.dialogCostProduct).text = product.Cost
            findViewById<TextView>(R.id.dialogDescriptionProduct).text = product.Discription

            // Обработчики кнопок
            findViewById<ImageView>(R.id.btnClose).setOnClickListener { dismiss() }
            findViewById<Button>(R.id.dialogAddToCart).setOnClickListener {
                onAddToCartClick(product)
                dismiss()
            }

            show()
        }
    }
}