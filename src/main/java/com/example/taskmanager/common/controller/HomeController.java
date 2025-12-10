package com.example.taskmanager.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ホーム画面のコントローラー.
 *
 * <p>ルートURLへのアクセスをタスク一覧にリダイレクトする。</p>
 */
@Controller
public class HomeController {

    /**
     * ルートURLからタスク一覧へリダイレクトする.
     *
     * @return タスク一覧へのリダイレクト先
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/tasks";
    }
}
