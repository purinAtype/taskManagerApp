package com.example.taskmanager.category.converter;

import com.example.taskmanager.category.dto.CategoryDto;
import com.example.taskmanager.category.form.CategoryForm;
import com.example.taskmanager.common.entity.TaskCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * カテゴリーのデータ変換インターフェース.
 *
 * <p>MapStructを使用して、Entity/DTO/Form間の相互変換を自動生成する。</p>
 */
@Mapper(componentModel = "spring")
public interface CategoryConverter {

    /**
     * EntityからDTOへ変換する.
     *
     * @param entity TaskCategoryエンティティ
     * @return CategoryDto
     */
    CategoryDto toDto(TaskCategory entity);

    /**
     * EntityリストからDTOリストへ変換する.
     *
     * @param entities TaskCategoryエンティティのリスト
     * @return CategoryDtoのリスト
     */
    List<CategoryDto> toDtoList(List<TaskCategory> entities);

    /**
     * FormからEntityへ変換する.
     * IDと作成日時・更新日時は自動設定されるため無視する。
     *
     * @param form CategoryForm
     * @return TaskCategoryエンティティ
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TaskCategory toEntity(CategoryForm form);

    /**
     * DTOからFormへ変換する.
     *
     * @param dto CategoryDto
     * @return CategoryForm
     */
    CategoryForm toForm(CategoryDto dto);

    /**
     * Formの内容で既存Entityを更新する.
     * IDと作成日時・更新日時は変更しないため無視する。
     *
     * @param form   CategoryForm
     * @param entity 更新対象のTaskCategoryエンティティ
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(CategoryForm form, @MappingTarget TaskCategory entity);
}
