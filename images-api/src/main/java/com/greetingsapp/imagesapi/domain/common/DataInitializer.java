package com.greetingsapp.imagesapi.domain.common;


import com.greetingsapp.imagesapi.domain.categories.Category;
import com.greetingsapp.imagesapi.domain.themes.Theme;
import com.greetingsapp.imagesapi.repository.CategoryRepository;
import com.greetingsapp.imagesapi.repository.ThemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//CLASE USADA PARA LA INICIALIZACION DE DATOS DE PRUEBA
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ThemeRepository themeRepository;

    //inicializacion
    @Override
    public void run(String... args) throws Exception {

        // SE VE VERIFICA SI LA BD ESTA VACIA, SINO LO ESTA SE CARGAN DATOS DE PRUEBA
        if (categoryRepository.count() == 0) {
            System.out.println("Cargando datos iniciales...");
            //inicializar categorias y tematicas
            initializerCategoriesAndThemes();
            System.out.println("Datos cargados.");
        }
    }

    //INICIALIZAR LAS CATEGORIAS
    private void initializerCategoriesAndThemes() {
        saveCategoryAndTheme("Buenos Días", "Feliz lunes");
        saveCategoryAndTheme("Navidad", "Happy new Year");
        saveCategoryAndTheme("Festividades", "Dia de las madres");
    }

    //metodo para guardar categorias y tematicas en la bd
    private void saveCategoryAndTheme(String categoryName, String themeName) {

        // Crea y guarda una categoría
        Category category = new Category();
        category.setName(categoryName);
        categoryRepository.save(category);

        // Crea una temática y la asocia a una categoría
        Theme theme = new Theme();
        theme.setName(themeName);
        theme.setCategory(category);
        themeRepository.save(theme);

    }
}

