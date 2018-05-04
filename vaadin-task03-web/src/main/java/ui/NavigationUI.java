package ui;

import com.vaadin.annotations.Title;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.PushStateNavigation;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.*;
import com.vaadin.spring.server.SpringVaadinServlet;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.ContextLoaderListener;
import views.DefaultView;
import views.categoryveiw.CategoryView;
import views.hotelveiw.HotelView;

import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;

@PushStateNavigation                    // allow separate URL with "/"
@Title("vaadin: task03")
@UIScope
@SpringUI
public class NavigationUI extends UI {
    public static String contextPath = null;
    public static Page startPage = null;

    @Autowired      // create been mainViewDisplay & set in Spring context
    private MainViewDisplay mainViewDisplay;
    @Autowired
    private MainMenu mainMenu;

    @Autowired
    private DefaultView defaultView;
    @Autowired
    private CategoryView categoryView;
    @Autowired
    private HotelView hotelView;

    public NavigationUI() {
        // delete after debug
        System.out.println("start -> NavigationUI. constructor");


        // delete after debug
        System.out.println("STOP -> NavigationUI. constructor");
    }

    @Override
    protected void init(VaadinRequest request) {
        // delete after debug
        System.out.println("start -> NavigationUI.init(VaadinRequest)");

        // determine context path
        if (contextPath == null) {
            contextPath = request.getContextPath();
            if (contextPath != null && !contextPath.isEmpty()) {
                // take contextPath without "/"
                contextPath = contextPath.substring(1) + "/";
            } else {
                // use root contextPath
                contextPath = "";
            }
            startPage = Page.getCurrent();
        }

        // delete after debug
        System.out.println("contextPath before pushState: " + contextPath);

        // scan event when URI changed by navigation in browser history
        getPage().addPopStateListener( e -> enter() );

        // set Navigator
        this.setNavigator(new Navigator(this, (ViewDisplay) mainViewDisplay));

        // Read the initial URI fragment
        enter();

        // delete after debug
        System.out.println("STOP -> NavigationUI.init(VaadinRequest)");
    }

    private void enter() {
//        ... initialize the UI ...
// this method is coled every time, as browser history were manipulated
        configureComponents();
        buildLayout();

    }
    private void configureComponents() {
        // delete after debug
        System.out.println("start -> NavigationUI.configureComponents");

        // clear all selections in mainMenu
        mainMenu.clearSelections();

        // register navigation views. As start -> send to defaultView
        UI.getCurrent().getNavigator().addView(contextPath, defaultView);
        UI.getCurrent().getNavigator().addView(DefaultView.VIEW_NAME, defaultView);

        UI.getCurrent().getNavigator().addView(CategoryView.VIEW_NAME, categoryView);
        UI.getCurrent().getNavigator().addView(contextPath + CategoryView.VIEW_NAME, categoryView);

        UI.getCurrent().getNavigator().addView(HotelView.VIEW_NAME, hotelView);
        UI.getCurrent().getNavigator().addView(contextPath + HotelView.VIEW_NAME, hotelView);

        // delete after debug
        System.out.println("STOP -> NavigationUI.configureComponents");
    }

    private void buildLayout() {
        final VerticalLayout root = new VerticalLayout();
        root.setSizeFull();
        setContent(root);

        // sen navigation bar - mainMenu
        root.addComponent(mainMenu);

        // mainViewDisplay this is Panel
        mainViewDisplay.setSizeFull();
        root.addComponent(mainViewDisplay);
        root.setExpandRatio(mainViewDisplay, 1.0f);
    }

    // configuration to use Spring
    @Configuration
    @EnableVaadin        // add vaadin configuration to create beans needed for vaadin in Spring context
    public static class MyConfiguration {
    }

    @WebListener        // to start Spring context - register
    public static class MyContextLoaderListener extends ContextLoaderListener {
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    public static class MyUIServlet extends SpringVaadinServlet {
    }


}
