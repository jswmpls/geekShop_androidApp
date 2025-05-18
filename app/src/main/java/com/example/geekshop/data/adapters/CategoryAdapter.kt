// Импорт необходимых классов Android и других зависимостей
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.geekshop.data.model.Category
import com.example.geekshop.R

// Класс адаптера для RecyclerView, который работает с категориями
class CategoryAdapter(
    // Исходный список категорий (по умолчанию пустой)
    private var categories: List<Category> = emptyList(),
    // Слушатель действий для обработки кликов по категориям
    private val listener: CategoryActionListener
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    // Интерфейс для обработки действий с категориями
    interface CategoryActionListener {
        // Вызывается при клике на категорию
        fun onCategoryClick(category: Category)
    }

    // Внутренний класс ViewHolder, который хранит ссылки на элементы представления
    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // TextView для отображения названия категории
        private val nameView: TextView = itemView.findViewById(R.id.titleCategory)

        // Метод для привязки данных категории к элементам представления
        fun bind(category: Category) {
            // Устанавливаем текст названия категории
            nameView.text = category.Title
            // Устанавливаем обработчик клика на весь элемент
            itemView.setOnClickListener { listener.onCategoryClick(category) }
        }
    }

    // Создает новый ViewHolder при необходимости
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        // Надуваем (inflate) макет для элемента категории из XML
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.style_category, parent, false)
        // Возвращаем новый ViewHolder с надутым представлением
        return CategoryViewHolder(view)
    }

    // Привязывает данные к ViewHolder на определенной позиции
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        // Вызываем метод bind у ViewHolder, передавая соответствующую категорию
        holder.bind(categories[position])
    }

    // Возвращает общее количество элементов в списке
    override fun getItemCount() = categories.size

    // Метод для обновления списка категорий с анимациями
    fun updateCategories(newCategories: List<Category>) {
        // Создаем callback для DiffUtil, который сравнивает старый и новый списки
        val diffCallback = CategoryDiffCallback(categories, newCategories)
        // Вычисляем различия между списками
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        // Обновляем список категорий
        categories = newCategories
        // Уведомляем RecyclerView об изменениях с анимациями
        diffResult.dispatchUpdatesTo(this)
    }

    // Внутренний класс для сравнения старых и новых данных с помощью DiffUtil
    private class CategoryDiffCallback(
        private val oldList: List<Category>,  // Старый список категорий
        private val newList: List<Category>   // Новый список категорий
    ) : DiffUtil.Callback() {

        // Возвращает размер старого списка
        override fun getOldListSize() = oldList.size

        // Возвращает размер нового списка
        override fun getNewListSize() = newList.size

        // Проверяет, являются ли элементы с указанными позициями одним и тем же объектом
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Сравниваем категории по названию (можно использовать ID, если он есть)
            return oldList[oldItemPosition].Title == newList[newItemPosition].Title
        }

        // Проверяет, одинаково ли содержимое элементов (при одинаковых объектах)
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Сравниваем все поля категорий
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}