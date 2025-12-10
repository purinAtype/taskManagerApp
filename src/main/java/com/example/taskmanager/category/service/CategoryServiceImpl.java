package com.example.taskmanager.category.service;

import com.example.taskmanager.category.converter.CategoryConverter;
import com.example.taskmanager.category.dto.CategoryDto;
import com.example.taskmanager.category.form.CategoryForm;
import com.example.taskmanager.category.mapper.CategoryCustomMapper;
import com.example.taskmanager.common.entity.TaskCategory;
import com.example.taskmanager.common.entity.TaskCategoryExample;
import com.example.taskmanager.common.exception.CategoryInUseException;
import com.example.taskmanager.common.exception.CategoryNotFoundException;
import com.example.taskmanager.common.mapper.TaskCategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * カテゴリー管理のサービス実装クラス.
 *
 * <p>カテゴリーのCRUD操作を提供する。
 * トランザクション管理を行い、データの整合性を保証する。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    /** カテゴリーマッパー */
    private final TaskCategoryMapper taskCategoryMapper;

    /** カテゴリーカスタムマッパー */
    private final CategoryCustomMapper categoryCustomMapper;

    /** カテゴリーコンバーター */
    private final CategoryConverter categoryConverter;

    /**
     * 全カテゴリーを表示順で取得する.
     *
     * @return カテゴリーDTOのリスト
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> findAll() {
        log.debug("Finding all categories");
        TaskCategoryExample example = new TaskCategoryExample();
        example.setOrderByClause("DISPLAY_ORDER ASC, NAME ASC");
        List<TaskCategory> categories = taskCategoryMapper.selectByExample(example);
        return categoryConverter.toDtoList(categories);
    }

    /**
     * IDを指定してカテゴリーを取得する.
     *
     * @param id カテゴリーID
     * @return カテゴリーDTO
     * @throws CategoryNotFoundException カテゴリーが見つからない場合
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryDto findById(Long id) {
        log.debug("Finding category by id: {}", id);
        TaskCategory category = taskCategoryMapper.selectByPrimaryKey(id);
        if (category == null) {
            throw new CategoryNotFoundException(id);
        }
        return categoryConverter.toDto(category);
    }

    /**
     * カテゴリーを新規作成する.
     *
     * @param form カテゴリーフォーム
     * @return 作成されたカテゴリーDTO
     */
    @Override
    public CategoryDto create(CategoryForm form) {
        log.debug("Creating category: {}", form.getName());
        TaskCategory category = categoryConverter.toEntity(form);
        taskCategoryMapper.insertSelective(category);
        log.info("Category created: id={}", category.getId());
        return categoryConverter.toDto(category);
    }

    /**
     * カテゴリーを更新する.
     *
     * @param id   更新するカテゴリーのID
     * @param form カテゴリーフォーム
     * @return 更新されたカテゴリーDTO
     * @throws CategoryNotFoundException カテゴリーが見つからない場合
     */
    @Override
    public CategoryDto update(Long id, CategoryForm form) {
        log.debug("Updating category: id={}", id);
        TaskCategory existingCategory = taskCategoryMapper.selectByPrimaryKey(id);
        if (existingCategory == null) {
            throw new CategoryNotFoundException(id);
        }
        categoryConverter.updateEntity(form, existingCategory);
        taskCategoryMapper.updateByPrimaryKeySelective(existingCategory);
        log.info("Category updated: id={}", id);
        return categoryConverter.toDto(existingCategory);
    }

    /**
     * カテゴリーを削除する.
     *
     * @param id 削除するカテゴリーのID
     * @throws CategoryNotFoundException カテゴリーが見つからない場合
     * @throws CategoryInUseException    カテゴリーが使用中の場合
     */
    @Override
    public void delete(Long id) {
        log.debug("Deleting category: id={}", id);
        TaskCategory existingCategory = taskCategoryMapper.selectByPrimaryKey(id);
        if (existingCategory == null) {
            throw new CategoryNotFoundException(id);
        }

        // カテゴリーが使用されているかチェック
        long taskCount = categoryCustomMapper.countTasksByCategoryId(id);
        if (taskCount > 0) {
            log.warn("Category is in use: id={}, taskCount={}", id, taskCount);
            throw new CategoryInUseException(id);
        }

        taskCategoryMapper.deleteByPrimaryKey(id);
        log.info("Category deleted: id={}", id);
    }
}
