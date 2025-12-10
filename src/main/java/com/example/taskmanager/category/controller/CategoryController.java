package com.example.taskmanager.category.controller;

import com.example.taskmanager.category.converter.CategoryConverter;
import com.example.taskmanager.category.dto.CategoryDto;
import com.example.taskmanager.category.form.CategoryForm;
import com.example.taskmanager.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * カテゴリー管理のコントローラー.
 *
 * <p>カテゴリーの一覧表示、登録、編集、削除を行う。</p>
 */
@Slf4j
@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    /** カテゴリーサービス */
    private final CategoryService categoryService;

    /** カテゴリーコンバーター */
    private final CategoryConverter categoryConverter;

    /**
     * カテゴリー一覧を表示する.
     *
     * @param model ビューに渡すモデル
     * @return カテゴリー一覧画面のビュー名
     */
    @GetMapping
    public String list(Model model) {
        log.debug("GET /categories");
        List<CategoryDto> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        return "category/list";
    }

    /**
     * カテゴリー新規登録フォームを表示する.
     *
     * @param model ビューに渡すモデル
     * @return カテゴリー登録フォーム画面のビュー名
     */
    @GetMapping("/new")
    public String newForm(Model model) {
        log.debug("GET /categories/new");
        model.addAttribute("categoryForm", CategoryForm.builder()
                .color("#6c757d")
                .displayOrder(0)
                .build());
        return "category/form";
    }

    /**
     * カテゴリーを新規登録する.
     *
     * @param categoryForm       登録するカテゴリーのフォームデータ
     * @param bindingResult      バリデーション結果
     * @param model              ビューに渡すモデル
     * @param redirectAttributes リダイレクト時に渡す属性
     * @return 成功時は一覧画面へリダイレクト、エラー時はフォーム画面のビュー名
     */
    @PostMapping
    public String create(@Valid @ModelAttribute CategoryForm categoryForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        log.debug("POST /categories - {}", categoryForm.getName());

        if (bindingResult.hasErrors()) {
            return "category/form";
        }

        categoryService.create(categoryForm);
        redirectAttributes.addFlashAttribute("successMessage", "カテゴリーを登録しました");
        return "redirect:/categories";
    }

    /**
     * カテゴリー編集フォームを表示する.
     *
     * @param id    編集するカテゴリーのID
     * @param model ビューに渡すモデル
     * @return カテゴリー編集フォーム画面のビュー名
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        log.debug("GET /categories/{}/edit", id);
        CategoryDto category = categoryService.findById(id);
        model.addAttribute("categoryForm", categoryConverter.toForm(category));
        model.addAttribute("categoryId", id);
        return "category/edit";
    }

    /**
     * カテゴリーを更新する.
     *
     * @param id                 更新するカテゴリーのID
     * @param categoryForm       更新するカテゴリーのフォームデータ
     * @param bindingResult      バリデーション結果
     * @param model              ビューに渡すモデル
     * @param redirectAttributes リダイレクト時に渡す属性
     * @return 成功時は一覧画面へリダイレクト、エラー時は編集フォーム画面のビュー名
     */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute CategoryForm categoryForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        log.debug("POST /categories/{} - {}", id, categoryForm.getName());

        if (bindingResult.hasErrors()) {
            model.addAttribute("categoryId", id);
            return "category/edit";
        }

        categoryService.update(id, categoryForm);
        redirectAttributes.addFlashAttribute("successMessage", "カテゴリーを更新しました");
        return "redirect:/categories";
    }

    /**
     * カテゴリーを削除する.
     *
     * @param id                 削除するカテゴリーのID
     * @param redirectAttributes リダイレクト時に渡す属性
     * @return カテゴリー一覧画面へのリダイレクト
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.debug("POST /categories/{}/delete", id);
        categoryService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "カテゴリーを削除しました");
        return "redirect:/categories";
    }
}
