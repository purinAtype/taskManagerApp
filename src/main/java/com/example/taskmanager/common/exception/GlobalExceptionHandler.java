package com.example.taskmanager.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * グローバル例外ハンドラー.
 *
 * <p>アプリケーション全体で発生する例外を捕捉し、
 * 適切なエラーページを表示する。</p>
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * タスク未発見例外を処理する.
     *
     * @param ex    発生した例外
     * @param model ビューに渡すモデル
     * @return 404エラーページのビュー名
     */
    @ExceptionHandler(TaskNotFoundException.class)
    public String handleTaskNotFound(TaskNotFoundException ex, Model model) {
        log.warn("Task not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    /**
     * カテゴリー未発見例外を処理する.
     *
     * @param ex    発生した例外
     * @param model ビューに渡すモデル
     * @return 404エラーページのビュー名
     */
    @ExceptionHandler(CategoryNotFoundException.class)
    public String handleCategoryNotFound(CategoryNotFoundException ex, Model model) {
        log.warn("Category not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    /**
     * カテゴリー使用中例外を処理する.
     *
     * @param ex                 発生した例外
     * @param model              ビューに渡すモデル
     * @param redirectAttributes リダイレクト時に渡す属性
     * @return カテゴリー一覧画面へのリダイレクト
     */
    @ExceptionHandler(CategoryInUseException.class)
    public String handleCategoryInUse(CategoryInUseException ex,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        log.warn("Category in use: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/categories";
    }

    /**
     * その他の例外を処理する.
     *
     * @param ex    発生した例外
     * @param model ビューに渡すモデル
     * @return 500エラーページのビュー名
     */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
        log.error("Unexpected error occurred", ex);
        model.addAttribute("errorMessage", "システムエラーが発生しました");
        return "error/500";
    }
}
