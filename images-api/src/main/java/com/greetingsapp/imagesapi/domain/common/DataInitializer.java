package com.greetingsapp.imagesapi.domain.common;

import com.greetingsapp.imagesapi.domain.categories.Category;
import com.greetingsapp.imagesapi.domain.specialdays.SpecialDay;
import com.greetingsapp.imagesapi.domain.themes.Theme;
import com.greetingsapp.imagesapi.repository.CategoryRepository;
import com.greetingsapp.imagesapi.repository.SpecialDayRepository;
import com.greetingsapp.imagesapi.repository.ThemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private SpecialDayRepository specialDayRepository;

    // Mapa para guardar las temáticas creadas y poder referenciarlas
    private Map<String, Theme> themeMap = new HashMap<>();

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            System.out.println("🔄 Cargando datos iniciales...");
            initializeCategoriesAndThemes();
            initializeSpecialDays();
            System.out.println("✅ Datos cargados exitosamente.");
        } else {
            System.out.println("ℹ️ Datos ya existentes. No se cargan datos iniciales.");
        }
    }

    private void initializeCategoriesAndThemes() {
        System.out.println("📂 Creando categorías y temáticas...");

        // ============================================
        // CATEGORÍA: Buenos Días
        // ============================================
        Category buenosDias = createCategory("Buenos Días");
        saveTheme("Feliz Lunes", buenosDias);
        saveTheme("Feliz Martes", buenosDias);
        saveTheme("Feliz Miércoles", buenosDias);
        saveTheme("Feliz Jueves", buenosDias);
        saveTheme("Feliz Viernes", buenosDias);
        saveTheme("Feliz Fin de Semana", buenosDias);
        saveTheme("Buenos Días Motivacionales", buenosDias);

        // ============================================
        // CATEGORÍA: Festividades
        // ============================================
        Category festividades = createCategory("Festividades");
        saveTheme("Navidad", festividades);
        saveTheme("Año Nuevo", festividades);
        saveTheme("Día de Reyes", festividades);
        saveTheme("Nochebuena", festividades);
        saveTheme("Fin de Año", festividades);
        saveTheme("Halloween", festividades);

        // ============================================
        // CATEGORÍA: Amor y Amistad
        // ============================================
        Category amorAmistad = createCategory("Amor y Amistad");
        saveTheme("San Valentín", amorAmistad);
        saveTheme("Día del Amor y la Amistad", amorAmistad);
        saveTheme("Aniversarios", amorAmistad);

        // ============================================
        // CATEGORÍA: Familia
        // ============================================
        Category familia = createCategory("Familia");
        saveTheme("Día de las Madres", familia);
        saveTheme("Día del Padre", familia);
        saveTheme("Cumpleaños", familia);

        // ============================================
        // CATEGORÍA: Patrias (República Dominicana)
        // ============================================
        Category patrias = createCategory("Patrias");
        saveTheme("Día de la Independencia", patrias);
        saveTheme("Día de la Restauración", patrias);
        saveTheme("Día de la Constitución", patrias);
        saveTheme("Día de las Mercedes", patrias);

        // ============================================
        // CATEGORÍA: Internacionales
        // ============================================
        Category internacionales = createCategory("Internacionales");
        saveTheme("Día Internacional de la Mujer", internacionales);
        saveTheme("Día de la Tierra", internacionales);
        saveTheme("Día del Trabajador", internacionales);

        System.out.println("✅ Categorías y temáticas creadas.");
    }

    private void initializeSpecialDays() {
        System.out.println("📅 Creando días especiales...");

        // ============================================
        // ENERO
        // ============================================
        saveSpecialDay("Año Nuevo", 1, 1, "Año Nuevo");
        saveSpecialDay("Día de Reyes", 6, 1, "Día de Reyes");

        // ============================================
        // FEBRERO
        // ============================================
        saveSpecialDay("Día del Amor y la Amistad", 14, 2, "Día del Amor y la Amistad");
        saveSpecialDay("Día de la Independencia", 27, 2, "Día de la Independencia");

        // ============================================
        // MARZO
        // ============================================
        saveSpecialDay("Día Internacional de la Mujer", 8, 3, "Día Internacional de la Mujer");

        // ============================================
        // ABRIL
        // ============================================
        saveSpecialDay("Día de la Tierra", 22, 4, "Día de la Tierra");

        // ============================================
        // MAYO
        // ============================================
        saveSpecialDay("Día del Trabajador", 1, 5, "Día del Trabajador");
        saveSpecialDay("Día de las Madres", 10, 5, "Día de las Madres");
        // Nota: Segundo domingo de mayo (aproximado día 10)

        // ============================================
        // JUNIO
        // ============================================
        saveSpecialDay("Día del Padre", 21, 6, "Día del Padre");
        // Nota: Último domingo de junio (aproximado día 21)

        // ============================================
        // AGOSTO
        // ============================================
        saveSpecialDay("Día de la Restauración", 16, 8, "Día de la Restauración");

        // ============================================
        // SEPTIEMBRE
        // ============================================
        saveSpecialDay("Día de las Mercedes", 24, 9, "Día de las Mercedes");

        // ============================================
        // OCTUBRE
        // ============================================
        saveSpecialDay("Halloween", 31, 10, "Halloween");

        // ============================================
        // NOVIEMBRE
        // ============================================
        saveSpecialDay("Día de la Constitución", 6, 11, "Día de la Constitución");

        // ============================================
        // DICIEMBRE
        // ============================================
        saveSpecialDay("Nochebuena", 24, 12, "Nochebuena");
        saveSpecialDay("Navidad", 25, 12, "Navidad");
        saveSpecialDay("Fin de Año", 31, 12, "Fin de Año");

        System.out.println("✅ Días especiales creados.");
    }

    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================

    private Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        return categoryRepository.save(category);
    }

    private void saveTheme(String themeName, Category category) {
        Theme theme = new Theme();
        theme.setName(themeName);
        theme.setCategory(category);
        Theme savedTheme = themeRepository.save(theme);

        // Guardamos la temática en el mapa para poder referenciarla después
        themeMap.put(themeName, savedTheme);
    }

    private void saveSpecialDay(String name, int day, int month, String themeName) {
        Theme theme = themeMap.get(themeName);

        if (theme == null) {
            System.err.println("⚠️ Advertencia: No se encontró la temática '" + themeName + "' para el día especial '" + name + "'");
            return;
        }

        SpecialDay specialDay = new SpecialDay();
        specialDay.setName(name);
        specialDay.setDay(day);
        specialDay.setMonth(month);
        specialDay.setTheme(theme);

        specialDayRepository.save(specialDay);
    }
}