package com.example.taskmanager.category.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * カテゴリーのカスタムマッパーインターフェース.
 *
 * <p>MyBatis Generatorで自動生成されないカスタムクエリを定義する。
 * SQLマッピングはresources/mapper/custom/category/CategoryCustomMapper.xmlで定義。</p>
 */
@Mapper
public interface CategoryCustomMapper {

    /**
     * 指定されたカテゴリーを使用しているタスクの件数を取得する.
     *
     * @param categoryId カテゴリーID
     * @return 使用しているタスクの件数
     */
    long countTasksByCategoryId(@Param("categoryId") Long categoryId);
}
