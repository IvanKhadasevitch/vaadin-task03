package views.categoryveiw;

import com.vaadin.data.Binder;
import com.vaadin.data.BinderValidationStatus;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import entities.Category;
import services.ICategoryService;
import utils.GetBeenFromSpringContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CategoryEditForm extends FormLayout {
    private static final Logger LOGGER = Logger.getLogger(CategoryEditForm.class.getName());

    private CategoryView categoryView;

    private ICategoryService categoryService;
    private Category category;
    private final Binder<Category> categoryBinder = new Binder<>(Category.class);

    private final TextField name = new TextField("Category name:");

    private final Button saveCategoryBtn = new Button("Save");
    private final Button closeFormBtn = new Button("Close");

    public CategoryEditForm(CategoryView categoryView){
        this.categoryView = categoryView;

        // get CategoryService bean
        this.categoryService = GetBeenFromSpringContext.getBeen(ICategoryService.class);

        this.setMargin(true);       // Enable layout margins. Affects all four sides of the layout
        this.setVisible(false);

        // form tools - buttons
        HorizontalLayout buttons = new HorizontalLayout(saveCategoryBtn, closeFormBtn);
        buttons.setSpacing(true);

        // collect form components - form fields & buttons
        this.addComponents(name, buttons);

        // add ToolTip to the forms fields
        name.setDescription("Category name");

        // connect entity fields with form fields
        name.setRequiredIndicatorVisible(true);         // Required field
        categoryBinder.forField(name)
                      .asRequired("Every category must have name")
                      .bind(Category::getName, Category::setName);

        // buttons
        saveCategoryBtn.setStyleName(ValoTheme.BUTTON_FRIENDLY);
        saveCategoryBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        saveCategoryBtn.addClickListener(e -> saveCategory());

        closeFormBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        closeFormBtn.addClickListener(e -> closeCategoryEditForm());
    }

    public void saveCategory() {
        // This will make all current validation errors visible
        BinderValidationStatus<Category> status = categoryBinder.validate();
        if (status.hasErrors()) {
            Notification.show("Validation error count: "
                    + status.getValidationErrors().size(), Notification.Type.WARNING_MESSAGE);
        }

        // save validated Category with not empty fields
        if ( !status.hasErrors() ) {
            // take validated data fields from binder to persisted category
            categoryBinder.writeBeanIfValid(this.category);

            // try save in DB new or update persisted category
            boolean isSaved = false;
            try {
                isSaved = categoryService.save(this.category) != null;
            } catch (Exception exp) {
                LOGGER.log(Level.WARNING, "Can't save category: " + this.category, exp);
            }

            if (isSaved) {
                // update categoryItems in hotel view
                this.categoryView.getHotelView().getHotelEditForm().updateCategoryItems();
                // update category view
                categoryView.updateCategoryList();
                this.setVisible(false);
                Notification.show("Saved category with name: " + this.category.getName(),
                        Notification.Type.HUMANIZED_MESSAGE);
            } else {
                Notification.show("Can't save category with name: " + this.category.getName(),
                        Notification.Type.ERROR_MESSAGE);
            }
        }
        categoryView.getAddCategoryBtn().setEnabled(true);
    }

    public void setCategory(Category category) {
        this.setVisible(true);

        // save persisted category in CategoryEditForm class
        this.category = category;

        // connect entity fields with form fields
        categoryBinder.readBean(category);
    }

    public void closeCategoryEditForm() {
        this.setVisible(false);
        categoryView.getAddCategoryBtn().setEnabled(true);
        categoryView.getCategoryList().deselectAll();
        categoryView.updateCategoryList();
    }
}
