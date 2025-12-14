package com.greetingsapp.imagesapi.services;

import com.greetingsapp.imagesapi.domain.images.Image;
import com.greetingsapp.imagesapi.domain.images.ImageMapper;
import com.greetingsapp.imagesapi.domain.themes.Theme;
import com.greetingsapp.imagesapi.dto.images.CreateImageDTO;
import com.greetingsapp.imagesapi.dto.images.ImageResponseDTO;
import com.greetingsapp.imagesapi.dto.images.UpdateImageDTO;
import com.greetingsapp.imagesapi.infra.errors.DuplicateResourceException;
import com.greetingsapp.imagesapi.infra.errors.ResourceNotFoundException;
import com.greetingsapp.imagesapi.repository.ImageRepository;
import com.greetingsapp.imagesapi.repository.ThemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ImageMapper imageMapper;


    @Transactional
    public ImageResponseDTO createImage(CreateImageDTO createImageDTO) {
        //validar que la tematica exista
        Theme theme = themeRepository.findById(createImageDTO.themeId())
                .orElseThrow(() -> new ResourceNotFoundException("Theme not found with id: " + createImageDTO.themeId()));

        Image newImage = new Image();
        newImage.setName(createImageDTO.name());
        newImage.setDescription(createImageDTO.description());
        newImage.setUrl(createImageDTO.url());
        newImage.setTheme(theme);// Asocia la imagen a su tematica.

        Image savedImage = imageRepository.save(newImage);

        return imageMapper.imageToImageResponseDTO(savedImage);
    }


    // --- Metodo para actualizar una imagen --- 🔄
    @Transactional
    public ImageResponseDTO updateImage(Long imageId, UpdateImageDTO updateImageDTO) {
        // 1. Busca la imagen que se va a actualizar.
        Image imageToUpdate = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));

        // 2. Busca la temática a la que se asignará la imagen.
        Theme theme = themeRepository.findById(updateImageDTO.themeId())
                .orElseThrow(() -> new ResourceNotFoundException("Theme not found with id: " + updateImageDTO.themeId()));

        // 3. --- VALIDACIÓN DE DUPLICIDAD ---
        // Busca si ya existe una imagen con el nuevo nombre en la nueva temática.
        Optional<Image> existingImage = imageRepository.findByNameAndTheme(updateImageDTO.name(), theme);

        // Si se encuentra una imagen y su ID es diferente al de la imagen que estamos actualizando,
        // entonces es un conflicto.
        if (existingImage.isPresent() && !existingImage.get().getId().equals(imageId)) {
            throw new DuplicateResourceException("Image with name '" + updateImageDTO.name() + "' already exists in this theme.");
        }

        // 4. Actualiza la entidad.
        imageToUpdate.setName(updateImageDTO.name());
        imageToUpdate.setDescription(updateImageDTO.description());
        imageToUpdate.setUrl(updateImageDTO.url());
        imageToUpdate.setTheme(theme);

        // 5. Guarda y mapea la respuesta.
        Image updatedImage = imageRepository.save(imageToUpdate);
        return imageMapper.imageToImageResponseDTO(updatedImage);
    }

    // --- Metodo para eliminar una imagen --- 🗑️
    @Transactional
    public void deleteImage(Long imageId) {
        if (!imageRepository.existsById(imageId)) {
            throw new ResourceNotFoundException("Image not found with id: " + imageId);
        }
        imageRepository.deleteById(imageId);
    }


    //usando para paginacion para traer las imagenes de una tematica especificada
    public Page<ImageResponseDTO> getImages(Long themeId, Pageable pageable) {

        // 1. VALIDACIÓN CORRECTA: ¿Existe la temática que nos piden?
        if (!themeRepository.existsById(themeId)) {
            // Si la temática NO EXISTE, esto SÍ es un error "No Encontrado".
            // Aquí sí lanzas tu excepción personalizada.
            throw new ResourceNotFoundException("Theme not found with id: " + themeId);
        }

        // 2. Si la temática existe, procede a buscar sus imágenes.
        Page<Image> imagePage = imageRepository.findByThemeId(themeId, pageable);

        // mapear la lista/pagina de imagenes retornada
        return imagePage.map(image -> imageMapper.imageToImageResponseDTO(image));
        // return imagePage.map(imageMapper::imageToImageResponseDTO);
    }

    //metodo usado para traer todas las imagenes con paginacion
    public Page<ImageResponseDTO> getAllImages(Pageable pageable) {
        Page<Image> imagePage = imageRepository.findAll(pageable);

        return imagePage.map(image -> imageMapper.imageToImageResponseDTO(image));
    }

}

