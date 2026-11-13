
package com.example.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UIControllerTest {

    private final UIController controller = new UIController();

    @Test
    void index_returnsIndexView() {
        String viewName = controller.index();

        assertEquals("index", viewName);
    }
}
