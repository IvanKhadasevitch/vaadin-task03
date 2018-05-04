package views.hotelveiw;

import com.vaadin.data.Binder;
import com.vaadin.data.BinderValidationStatus;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.data.validator.DateRangeValidator;
import com.vaadin.server.SerializableFunction;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import entities.Category;
import entities.Hotel;
import services.ICategoryService;
import services.IHotelService;
import utils.GetBeenFromSpringContext;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HotelEditForm extends FormLayout {
    private static final Logger LOGGER = Logger.getLogger(HotelEditForm.class.getName());

    private HotelView hotelView;

    private IHotelService hotelService;
    private ICategoryService categoryService;

    private Hotel hotel = new Hotel();
    private Binder<Hotel> hotelBinder = new Binder<>(Hotel.class);

    private TextField name = new TextField("Name:");
    private TextField address = new TextField("Address:");
    private TextField rating = new TextField("Rating:");
    private DateField operatesFrom = new DateField("Operates from:");
    private NativeSelect<String> category = new NativeSelect<>("Category:");
    private TextField url = new TextField("URL:");
    private TextArea description = new TextArea("Description:");

    private Button saveHotelBtn = new Button("Save");
    private Button closeFormBtn = new Button("Close");

    public HotelEditForm(HotelView hotelView) {
        this.hotelView = hotelView;

        // take beans ICategoryService & IHotelService
        this.hotelService = GetBeenFromSpringContext.getBeen(IHotelService.class);
        this.categoryService = GetBeenFromSpringContext.getBeen(ICategoryService.class);

        this.setMargin(true);       // Enable layout margins. Affects all four sides of the layout
        this.setVisible(false);     // hide form at start

        HorizontalLayout buttons = new HorizontalLayout(saveHotelBtn, closeFormBtn);
        buttons.setSpacing(true);

        this.addComponents(name, address, rating, operatesFrom, category,
                url, description, buttons);

        // add ToolTip to the forms fields
        name.setDescription("Hotel name");
        name.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        address.setDescription("Hotel address");
        address.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        rating.setDescription("Hotel rating from 0 to 5 stars");
        rating.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        operatesFrom.setDescription("Date of the beginning of the operating " +
                "of the hotel must be in the past");
        operatesFrom.addStyleName(ValoTheme.DATEFIELD_SMALL);
        category.setDescription("Hotel category");
        category.addStyleName(ValoTheme.COMBOBOX_SMALL);
        url.setDescription("Info about hotel on the booking.com");
        url.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        description.setDescription("Hotel description");
        description.setStyleName(ValoTheme.TEXTAREA_SMALL);

        // connect entity fields with form fields
        name.setRequiredIndicatorVisible(true);         // Required field
        hotelBinder.forField(name)
                   // Shorthand for requiring the field to be non-empty
                   .asRequired("Every hotel must have a name")
                   .bind(Hotel::getName, Hotel::setName);

        address.setRequiredIndicatorVisible(true);
        hotelBinder.forField(address)
                   .asRequired("Every hotel must have a address")
                   .bind(Hotel::getAddress, Hotel::setAddress);

        rating.setRequiredIndicatorVisible(true);
        hotelBinder.forField(rating)
                   .asRequired("Every hotel must have a rating")
                   .withConverter(new StringToIntegerConverter("Enter an integer, please"))
                   .withValidator(rating -> rating >= 0 && rating <= 5,
                           "Rating must be between 0 and 5")
                   .bind(Hotel::getRating, Hotel::setRating);

        operatesFrom.setRequiredIndicatorVisible(true);
        hotelBinder.forField(operatesFrom)
                   .asRequired("Every hotel must operates from a certain date")
                   .withValidator(new DateRangeValidator("Date must be in the past",
                           null, LocalDate.now().minusDays(1)))
                   .withConverter(LocalDate::toEpochDay, LocalDate::ofEpochDay,
                           "Don't look like a date")
                   .bind(Hotel::getOperatesFrom, Hotel::setOperatesFrom);

        category.setRequiredIndicatorVisible(true);
//        SerializableFunction<String, Category> toModel = (categoryService::getCategoryByName);
        SerializableFunction<String, Category> toModel = (this::getCategoryByName);
        SerializableFunction<Category, String> toPresentation = (category -> {
            return category != null ? category.getName() : "";
        });
        SerializablePredicate<? super String> isCategoryNameInList = (this::isCategoryNameInList
//        {
//            boolean result = categoryService.existWithName(categoryName);
//
//            // delete after debug
//            System.out.println("category: " + categoryName + " ---> exist in DB: " +result);
//
//            return result;
//        }

        );
        hotelBinder.forField(category)
                   .asRequired("Every hotel must have a category")
                   .withValidator(isCategoryNameInList, "Define category, please")
                   .withConverter(toModel, toPresentation, "No such category")
                   .bind(Hotel::getCategory, Hotel::setCategory);

        url.setRequiredIndicatorVisible(true);
        hotelBinder.forField(url)
                   .asRequired("Every hotel must have a link to booking.com")
                   .bind(Hotel::getUrl, Hotel::setUrl);

        hotelBinder.forField(description).bind(Hotel::getDescription, Hotel::setDescription);

        // fill categories items
        updateCategoryItems();

        // buttons
        saveHotelBtn.setStyleName(ValoTheme.BUTTON_FRIENDLY);
        saveHotelBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        saveHotelBtn.addClickListener(e -> saveHotel());

        closeFormBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        closeFormBtn.addClickListener(e -> closeHotelEditForm());
    }

    public void saveHotel() {
        // This will make all current validation errors visible
        BinderValidationStatus<Hotel> status = hotelBinder.validate();
        if (status.hasErrors()) {
            Notification.show("Validation error count: "
                    + status.getValidationErrors().size(), Notification.Type.WARNING_MESSAGE);
        }

        // save validated hotel with not empty fields (exclude description)
        if (!status.hasErrors()) {

            // !!!!!!!!!!!!
            // take validated data fields from binder to persisted category
            hotelBinder.writeBeanIfValid(this.hotel);
//            // try save in DB new or update persisted hotel
            boolean isSaved = false;
            try {
                isSaved = hotelService.save(this.hotel) != null;
            }catch (Exception exp) {
                LOGGER.log(Level.WARNING, "Can't save hotel: " + this.hotel, exp);
            }

            if (isSaved) {
                hotelView.updateHotelList();
                this.setVisible(false);

                hotelView.getAddHotelBtn().setEnabled(true);

                Notification.show("Saved hotel with name: " + hotel.getName(),
                        Notification.Type.HUMANIZED_MESSAGE);
            } else {
                Notification.show(String.format("Can't save hotel with name [%s]. Close form & try again ",
                        hotel.getName()), Notification.Type.ERROR_MESSAGE);
            }
        }
//        hotelView.getAddHotelBtn().setEnabled(true);

        // update Category Items from DB to pick up changes
        updateCategoryItems();
    }

    public void setHotel(Hotel hotel) {
        this.setVisible(true);
        this.hotel = hotel;

        // connect entity fields with form fields
        // !!!!!!!!!!!!-------------
        hotelBinder.readBean(hotel);

        // refresh category items
        updateCategoryItems();
        // set active category in category items list
        category.setValue(hotel.getCategory() != null ? hotel.getCategory().getName() : "");
    }

    public void closeHotelEditForm() {
        this.setVisible(false);
        hotelView.getAddHotelBtn().setEnabled(true);
        hotelView.getHotelList().deselectAll();
        hotelView.updateHotelList();
    }

    public void updateCategoryItems() {
        try {
            // fill categories items
            List<String> categoryItems = categoryService.getAll()
                                                        .stream()
                                                        .map(Category::getName)
                                                        .collect(Collectors.toList());
            category.setItems(categoryItems);
            category.setValue(hotel.getCategory() !=null ? hotel.getCategory().getName() : "");
        } catch (Exception exp) {
            LOGGER.log(Level.WARNING, "Can't get all categories from Db", exp);
            Notification.show("Connection with DB were lost. Try again.",
                    Notification.Type.ERROR_MESSAGE);
        }

    }

    private Category getCategoryByName(String categoryName) {
        Category result = null;
        try {
            result = categoryService.getCategoryByName(categoryName);
        } catch (Exception exp) {
            LOGGER.log(Level.WARNING, "Can't get from Db category by name: " + categoryName, exp);
            Notification.show("Connection with DB were lost. Try again.",
                    Notification.Type.ERROR_MESSAGE);
        }

        return result;
    }

    private boolean isCategoryNameInList(String categoryName) {
        boolean result = false;
        try {
            result = categoryService.existWithName(categoryName);
        } catch (Exception exp) {
            LOGGER.log(Level.WARNING, "Can't take from DB category with name: " + categoryName, exp);
        }

        return result;
    }
}
