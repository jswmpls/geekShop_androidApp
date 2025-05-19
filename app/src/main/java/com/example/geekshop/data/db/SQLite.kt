package com.example.geekshop.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.geekshop.data.model.Products
import com.example.geekshop.data.model.Users
import com.example.geekshop.R

class SQLite private constructor(context: Context) : SQLiteOpenHelper(context, "GeekShop", null, 4) {

    companion object {
        @Volatile
        private var instance: SQLite? = null

        // Получение экземпляра БД (потокобезопасно)
        fun getInstance(context: Context): SQLite {
            return instance ?: synchronized(this) {
                instance ?: SQLite(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Создание таблиц при первом запуске приложения:
     * - products - таблица товаров
     * - users - таблица пользователей
     * - cart - таблица корзины покупок
     */
    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL("""
            CREATE TABLE products (
                id INTEGER PRIMARY KEY, 
                name TEXT, 
                cost TEXT, 
                imageId INTEGER, 
                description TEXT, 
                category TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY, 
                name TEXT, 
                login TEXT UNIQUE, 
                password TEXT, 
                bonus INTEGER DEFAULT 0
            )
        """)

        db.execSQL("""
            CREATE TABLE cart (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                product_id INTEGER,
                quantity INTEGER DEFAULT 1,
                FOREIGN KEY(user_id) REFERENCES users(id),
                FOREIGN KEY(product_id) REFERENCES products(id)
            )
        """)
    }

    /**
     * Обновление базы данных при изменении версии:
     * Удаляем старые таблицы и создаем заново
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS products")
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS cart")
        onCreate(db)
    }

    // Методы для работы с товарами

    /**
     * Получение всех товаров из базы
     * @return Список всех товаров
     */
    fun getAllProducts(): List<Products> {
        val products = mutableListOf<Products>()

        readableDatabase.rawQuery("SELECT * FROM products", null).use { cursor ->
            while (cursor.moveToNext()) {
                products.add(
                    Products(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        Name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        Cost = cursor.getString(cursor.getColumnIndexOrThrow("cost")),
                        inCart = false,
                        imageId = cursor.getInt(cursor.getColumnIndexOrThrow("imageId")),
                        Discription = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        Category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
                    )
                )
            }
        }
        return products
    }

    // Методы для работы с пользователями

    fun isLoginExists(login: String): Boolean {
        return readableDatabase.rawQuery(
            "SELECT id FROM users WHERE login = ?",
            arrayOf(login)
        ).use { it.count > 0 }
    }

    fun getNextUserId(): Int {
        return readableDatabase.rawQuery(
            "SELECT MAX(id) FROM users",
            null
        ).use { cursor ->
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                cursor.getInt(0) + 1
            } else {
                1
            }
        }
    }

    fun getUser(login: String, password: String): Boolean {
        return readableDatabase.rawQuery(
            "SELECT * FROM users WHERE login = ? AND password = ? LIMIT 1",
            arrayOf(login, password)
        ).use { it.count > 0 }
    }

    fun getUserData(login: String): Users {
        return readableDatabase.rawQuery(
            "SELECT * FROM users WHERE login = ? LIMIT 1",
            arrayOf(login)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                Users(
                    id = cursor.getInt(0),
                    name = cursor.getString(1),
                    login = cursor.getString(2),
                    password = "",
                    bonus = cursor.getInt(4)
                )
            } else {
                throw Exception("Пользователь не найден")
            }
        }
    }

    // Методы для работы с корзиной

    /**
     * Добавление товара в корзину пользователя
     * @param userId ID пользователя
     * @param productId ID товара
     * @return true если операция успешна
     */
    fun addToCart(userId: Int, productId: Int): Boolean {
        val db = this.writableDatabase
        return try {
            db.beginTransaction()

            // Проверка существования товара
            val productExists = db.rawQuery(
                "SELECT id FROM products WHERE id = ?",
                arrayOf(productId.toString())
            ).use { it.count > 0 }

            if (!productExists) {
                Log.e("SQLite", "Product $productId not found")
                return false
            }

            // Проверка наличия товара в корзине
            val cursor = db.rawQuery(
                "SELECT id, quantity FROM cart WHERE user_id = ? AND product_id = ?",
                arrayOf(userId.toString(), productId.toString())
            )

            if (cursor.moveToFirst()) {
                // Увеличиваем количество, если товар уже в корзине
                val newQuantity = cursor.getInt(1) + 1
                db.execSQL(
                    "UPDATE cart SET quantity = $newQuantity WHERE id = ${cursor.getInt(0)}"
                )
            } else {
                // Добавляем новый товар в корзину
                db.execSQL(
                    "INSERT INTO cart (user_id, product_id, quantity) VALUES ($userId, $productId, 1)"
                )
            }
            cursor.close()

            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            Log.e("SQLite", "Error adding to cart", e)
            false
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Получение содержимого корзины пользователя
     * @param userId ID пользователя
     * @return Список пар (товар, количество)
     */
    fun getCartProducts(userId: Int): List<Pair<Products, Int>> {
        return readableDatabase.rawQuery("""
            SELECT p.id, p.name, p.cost, p.imageId, p.description, p.category, c.quantity 
            FROM cart c 
            JOIN products p ON c.product_id = p.id 
            WHERE c.user_id = ?
        """, arrayOf(userId.toString())).use { cursor ->
            val items = mutableListOf<Pair<Products, Int>>()
            while (cursor.moveToNext()) {
                items.add(
                    Products(
                        id = cursor.getInt(0),
                        Name = cursor.getString(1),
                        Cost = cursor.getString(2),
                        inCart = false,
                        imageId = cursor.getInt(3),
                        Discription = cursor.getString(4),
                        Category = cursor.getString(5)
                    ) to cursor.getInt(6)
                )
            }
            items
        }
    }

    /**
     * Удаление товара из корзины
     * @param userId ID пользователя
     * @param productId ID товара
     */
    fun removeFromCart(userId: Int, productId: Int) {
        writableDatabase.delete(
            "cart",
            "user_id = ? AND product_id = ?",
            arrayOf(userId.toString(), productId.toString())
        )
    }

    /**
     * Очистка корзины пользователя
     * @param userId ID пользователя
     */
    fun clearCart(userId: Int) {
        writableDatabase.delete(
            "cart",
            "user_id = ?",
            arrayOf(userId.toString())
        )
    }

    // Методы для работы с бонусами

    /**
     * Получение количества бонусов пользователя
     * @param userId ID пользователя
     * @return Количество бонусов
     */
    fun getUserBonus(userId: Int): Int {
        return readableDatabase.rawQuery(
            "SELECT bonus FROM users WHERE id = ?",
            arrayOf(userId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    /**
     * Обновление количества бонусов пользователя
     * @param userId ID пользователя
     * @param newBonus Новое количество бонусов
     */
    fun updateUserBonus(userId: Int, newBonus: Int) {
        val values = ContentValues().apply {
            put("bonus", newBonus)
        }
        writableDatabase.update(
            "users",
            values,
            "id = ?",
            arrayOf(userId.toString())
        )
    }

    /**
     * Получение начального списка товаров для заполнения базы данных
     * @return Список товаров по умолчанию
     */
        fun getInitialProducts(): List<Products> {
            return listOf(
                //AAA-игры
                Products(
                    id = 1,
                    Name = "Call of Duty: Modern Warfare II",
                    Cost = "1999 ₽",
                    inCart = false,
                    imageId = R.drawable.image_call_of_duty,
                    Discription = "В прямом продолжении культовой игры Call of Duty: Modern Warfare II капитан Прайс и ОТГ-141 сражаются со своим главным противником. Им предстоит вести беспощадный бой, чтобы ультранационалист и военный преступник Владимир Макаров не смог распространить свое влияние на весь мир.",
                    Category = "AAA-игры"
                ),
                Products(
                    id = 2,
                    Name = "Cyberpunk 2077",
                    Cost = "1999 ₽",
                    inCart = false,
                    imageId = R.drawable.image_cyberpunk,
                    Discription = "Откройте для себя мир киберпанка во всём его многообразии, от многогранной истории Cyberpunk 2077 и шпионских интриг в дополнении «Призрачная свобода» до трогательных эпизодов популярного аниме Cyberpunk: Edgerunners, завоевавшего множество наград. Опасный мегаполис Найт-Сити всегда найдёт, чем вас удивить.",
                    Category = "AAA-игры"
                ),
                Products(
                    id = 3,
                    Name = "Elden Ring",
                    Cost = "3999 ₽",
                    inCart = false,
                    imageId = R.drawable.image_elden_ring,
                    Discription = "Ролевой экшен от создателей Dark Souls и Джорджа Мартина, предлагающий исследование огромного открытого мира Срединных земель. Игроки выбирают один из классов и отправляются в путешествие, сражаясь с легендарными боссами, находя тайные подземелья и собирая осколки Кольца Элден. Отличительные черты — полная свобода передвижения, система верховой езды и глубокий кастомизации боевых стилей.",
                    Category = "AAA-игры"
                ),
                Products(
                    id = 4,
                    Name = "Starfield",
                    Cost = "999 ₽",
                    inCart = false,
                    imageId = R.drawable.image_statfield,
                    Discription = "Масштабная космическая RPG от Bethesda, действие которой разворачивается в галактике с более чем 1000 планет. Игроки становятся членом организации Исследователи и могут свободно путешествовать между системами, строить аванпосты, участвовать в космических битвах и заключать союзы с различными фракциями. Особое внимание уделено кастомизации кораблей и исследованию тайн древней цивилизации.",
                    Category = "AAA-игры"
                ),
                Products(
                    id = 5,
                    Name = "FIFA 24",
                    Cost = "2999 ₽",
                    inCart = false,
                    imageId = R.drawable.image_fifa2024,
                    Discription = "Последняя часть футбольного симулятора от EA Sports с улучшенной физикой HyperMotionV, которая делает движения игроков более реалистичными. В режиме карьеры появилась возможность управлять не только клубом, но и целой лигой, а в Ultimate Team добавлены женские команды. Игра использует технологию машинного обучения для более интеллектуального ИИ соперников.",
                    Category = "AAA-игры"
                ),
                Products(
                    id = 6,
                    Name = "Assassin’s Creed Mirage",
                    Cost = "999 ₽",
                    inCart = false,
                    imageId = R.drawable.image_assassin,
                    Discription = "Возвращение к корням серии с акцентом на скрытность и паркур. Действие происходит в Багдаде IX века, где игроки управляют юным вором Басимом, который становится членом Братства ассасинов. Игра предлагает компактный, но насыщенный город с множеством возможностей для скрытных убийств и побегов, а также новую систему контрактов.",
                    Category = "AAA-игры"
                ),
                Products(
                    id = 7,
                    Name = "Avatar: Frontiers of Pandora",
                    Cost = "1499 ₽",
                    inCart = false,
                    imageId = R.drawable.image_avatar,
                    Discription = "Экшен-адвенчура от создателей The Division, где игроки становятся На’ви и исследуют неизведанные регионы Пандоры. Игра делает упор на исследование открытого мира с уникальной экосистемой, сражения с силами RDA с использованием традиционного оружия На’ви и верхом на икранах. Особенностью является система взаимодействия с окружающей средой, где каждое действие влияет на экологический баланс.",
                    Category = "AAA-игры"
                ),

                // VR
                Products(
                    id = 8,
                    Name = "Half-Life: Alyx",
                    Cost = "2999 ₽",
                    inCart = false,
                    imageId = R.drawable.image_half_life,
                    Discription = "Революционный шутер от Valve, созданный исключительно для VR, который переносит игроков во вселенную Half-Life между событиями первой и второй части. Игра предлагает невероятно детализированную среду, инновационную систему взаимодействия с предметами и напряженный сюжет, где Аликс Вэнс сражается с Альянсом.",
                    Category = "VR"
                ),
                Products(
                    id = 9,
                    Name = "Beat Saber",
                    Cost = "599 ₽",
                    inCart = false,
                    imageId = R.drawable.image_beat_saber,
                    Discription = "Ритм-игра, ставшая визитной карточкой VR, где игроки используют световые мечи, чтобы разрушать летящие в такт музыке блоки, что создает эффект полного погружения в музыкальный поток.",
                    Category = "VR"
                ),
                Products(
                    id = 10,
                    Name = "Resident Evil 4 VR",
                    Cost = "3999 ₽",
                    inCart = false,
                    imageId = R.drawable.image_resident_evil_4,
                    Discription = "Адаптация культового хоррора, переосмысленная для виртуальной реальности, где каждый поворот головы, прицеливание и перезарядка оружия ощущаются по-новому, а атмосфера деревни и её жутких обитателей становится ещё более пугающей. ",
                    Category = "VR"
                ),
                Products(
                    id = 11,
                    Name = "Superhot VR",
                    Cost = "799 ₽",
                    inCart = false,
                    imageId = R.drawable.image_supershot,
                    Discription = "Инновационный шутер, где время движется только тогда, когда двигается игрок, что создает эффект «матрицы» и требует стратегического мышления для устранения врагов в минималистичном, но стильном мире.",
                    Category = "VR"
                ),
                Products(
                    id = 12,
                    Name = "The Walking Dead: Saints & Sinners",
                    Cost = "1599 ₽",
                    inCart = false,
                    imageId = R.drawable.image_the_walking_dead,
                    Discription = "Мрачный и напряженный survival-хоррор, где игроки выживают в затопленном Новом Орлеане, полном зомби, принимая трудные моральные решения, собирая ресурсы и сражаясь за свою жизнь с помощью физического боя и импровизированного оружия. Каждая из этих игр демонстрирует потенциал VR, предлагая уникальные механики и незабываемые впечатления, которые невозможно повторить в традиционных играх.",
                    Category = "VR"
                ),

                // Free-To-Play
                Products(
                    id = 13,
                    Name = "Fortnite",
                    Cost = "0 ₽",
                    inCart = false,
                    imageId = R.drawable.image_fortnite,
                    Discription = "Культурный феномен, сочетающий яркий королевский бой с постоянными сюжетными событиями и кроссоверами с популярными франшизами; его строительная механика и регулярные обновления делают игру постоянно свежей.",
                    Category = "Free-To-Play"
                ),
                Products(
                    id = 14,
                    Name = "Apex Legends",
                    Cost = "0 ₽",
                    inCart = false,
                    imageId = R.drawable.image_apex,
                    Discription = "Динамичный шутер от создателей Titanfall с уникальными персонажами-легендами, каждый из которых обладает особыми способностями, что создаёт стратегическую глубину в командных сражениях на арене; игра славится продуманной системой пингов и плавным движением.",
                    Category = "Free-To-Play"
                ),
                Products(
                    id = 15,
                    Name = "Warframe",
                    Cost = "0 ₽",
                    inCart = false,
                    imageId = R.drawable.image_half_life,
                    Discription = "Научно-фантастический экшен с элементами RPG, где игроки управляют древними воинами Тэнно, выполняя миссии в открытом мире и кастомизируя своего персонажа сотнями способов; игра отличается сложной системой прокачки и кооперативным геймплеем.",
                    Category = "Free-To-Play"
                ),
                Products(
                    id = 16,
                    Name = "Valorant",
                    Cost = "0 ₽",
                    inCart = false,
                    imageId = R.drawable.image_valorant,
                    Discription = "Valorant сочетает точность тактических шутеров в духе CS:GO с уникальными способностями агентов, создавая напряжённые соревновательные матчи; игра делает упор на командную работу и стратегическое планирование.",
                    Category = "Free-To-Play"
                ),
                Products(
                    id = 17,
                    Name = "Genshin Impact",
                    Cost = "0 ₽",
                    inCart = false,
                    imageId = R.drawable.image_genshin,
                    Discription = "Красочная action-RPG с открытым миром, вдохновлённая аниме-эстетикой, где игроки исследуют волшебный мир Тейват, собирают персонажей с разными элементами и комбинируют их способности в динамичных сражениях; проект славится потрясающей графикой, глубоким сюжетом и регулярным добавлением нового контента.",
                    Category = "Free-To-Play"
                ),

                // Эксклюзивы
                Products(
                    id = 19,
                    Name = "The Last of Us Part II",
                    Cost = "4999 ₽",
                    inCart = false,
                    imageId = R.drawable.image_the_last_of_us,
                    Discription = "Погружает в мрачный постапокалиптический мир, где эмоциональная история мести и искупления переплетается с напряженным стелс-геймплеем и жестокими схватками.",
                    Category = "Эксклюзивы"
                ),
                Products(
                    id = 20,
                    Name = "Forza Horizon 5",
                    Cost = "2599 ₽",
                    inCart = false,
                    imageId = R.drawable.image_forza5,
                    Discription = "Устанавливает новые стандарты гоночных симуляторов с потрясающе детализированной Мексикой, где игроки могут свободно исследовать разнообразные ландшафты, участвовать в сотнях событий и коллекционировать сотни лицензированных автомобилей.",
                    Category = "Эксклюзивы"
                ),
                Products(
                    id = 21,
                    Name = "Super Mario Odyssey",
                    Cost = "799 ₽",
                    inCart = false,
                    imageId = R.drawable.image_mario,
                    Discription = "Виртуозное воплощение платформера от Nintendo, где Марио с помощью своей живой шляпы Кэппи путешествует по красочным королевствам, демонстрируя безграничную фантазию геймдизайна и радость открытий.",
                    Category = "Эксклюзивы"
                ),
                Products(
                    id = 22,
                    Name = "God of War: Ragnarök",
                    Cost = "2499 ₽",
                    inCart = false,
                    imageId = R.drawable.image_god_of_war,
                    Discription = "Завершает скандинавскую сагу Кратоса и Атрея эпическим путешествием по Девяти мирам, сочетая мощные сражения с глубоким повествованием и потрясающими визуальными эффектами.",
                    Category = "Эксклюзивы"
                ),
                Products(
                    id = 23,
                    Name = "Uncharted 4: A Thief’s End",
                    Cost = "1999 ₽",
                    inCart = false,
                    imageId = R.drawable.image_uncharted,
                    Discription = "Представляет собой идеально сбалансированное приключение Нейтана Дрейка с кинематографичными сценами, головокружительными паркур-секвенциями и увлекательными головоломками, ставшее эталоном для жанра экшен-адвенчур. ",
                    Category = "Эксклюзивы"
                ),

                // Инди-игры
                Products(
                    id = 25,
                    Name = "Hollow Knight: Silksong",
                    Cost = "ожидается",
                    inCart = false,
                    imageId = R.drawable.image_hollow_knifgt,
                    Discription = "Долгожданное продолжение культового метроидвании, где игроки возьмут на себя роль Хорнет, отправляющейся в опасное путешествие по загадочному королевству Фарлум; игра обещает сохранить фирменную атмосферу меланхоличной красоты и сложные босс-файты, дополнив их новыми механиками и более динамичным геймплеем.",
                    Category = "Инди-игры"
                ),
                Products(
                    id = 26,
                    Name = "Stardew Valley",
                    Cost = "799 ₽",
                    inCart = false,
                    imageId = R.drawable.image_stardew_valley,
                    Discription = "Уютный фермерский симулятор с элементами RPG, где можно не только выращивать урожай и разводить животных, но и исследовать пещеры, заводить отношения с жителями городка Пеликан и восстанавливать заброшенную ферму, создавая собственный уголок рая; игра покоряет своей искренностью, вниманием к деталям и безграничными возможностями для творчества.",
                    Category = "Инди-игры"
                ),
                Products(
                    id = 27,
                    Name = "Hades",
                    Cost = "399 ₽",
                    inCart = false,
                    imageId = R.drawable.image_hades,
                    Discription = "Динамичный roguelike-экшен от студии Supergiant Games, где игроки берут на себя роль Загрея, бессмертного принца Подземного мира, который пытается сбежать из владений своего отца Аида. Игра впечатляет идеально отточенным геймплеем — каждый побег представляет собой череду комнат с разными типами испытаний, где нужно сражаться с мифологическими существами, используя разнообразное оружие и дары олимпийских богов, которые предоставляют уникальные способности.",
                    Category = "Инди-игры"
                ),

                // Классика
                Products(
                    id = 28,
                    Name = "Diablo II: Resurrected",
                    Cost = "1799 ₽",
                    inCart = false,
                    imageId = R.drawable.image_diablo,
                    Discription = "Тщательно обновлённая версия легендарного хак-н-слэша 2000 года, где игроки выбирают один из семи классов и отправляются в тёмный фэнтезийный мир, чтобы сразиться с Повелителями Ужаса. Ремастер сохранил культовый геймплей с его глубокой системой прокачки и бесконечным лутанием, но добавил современную 4K-графику с переработанными моделями персонажей, анимациями и эффектами, а также улучшенный интерфейс и поддержку геймпадов.",
                    Category = "Классика"
                ),
                Products(
                    id = 29,
                    Name = "Final Fantasy VII Remake",
                    Cost = "2399 ₽",
                    inCart = false,
                    imageId = R.drawable.image_final_fantasy_7,
                    Discription = "Амбициозная переработка классической JRPG 1997 года, которая переосмысливает оригинальный сюжет, расширяя историю Клауда, Тифы и Эйриса с новыми сюжетными поворотами и персонажами. Игра сочетает пошаговую систему боя с динамичными реальными сражениями, где важно переключаться между героями и грамотно использовать их уникальные способности. ",
                    Category = "Классика"
                ),
                Products(
                    id = 30,
                    Name = "The Witcher 3: Wild Hunt – Complete Edition",
                    Cost = "1899 ₽",
                    inCart = false,
                    imageId = R.drawable.image_the_witcher_3,
                    Discription = "Включает основную игру и все дополнения, представляя собой одну из самых богатых и детализированных open-world RPG. В роли Геральта из Ривии игроки путешествуют по огромному миру, полному морально сложных квестов, запоминающихся персонажей и опасных монстров. Издание включает графические улучшения для новых поколений консолей, такие как трассировка лучей, более высокое разрешение текстуры и улучшенную производительность, делая игру визуально ещё более впечатляющей. Каждая из этих игр не просто обновляет классику, но и предлагает современный игровой опыт, оставаясь верной духу оригиналов.",
                    Category = "Классика"
                )
            )
        }
        }