package views.hotelveiw;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.themes.ValoTheme;
import entities.Category;
import entities.Hotel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import services.ICategoryService;
import services.IHotelService;
import ui.MainViewDisplay;
import ui.NavigationUI;
import ui.customcompanents.FilterWithClearBtn;
import utils.GetBeenFromSpringContext;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@UIScope
@SpringView(name = HotelView.VIEW_NAME)
public class HotelView extends VerticalLayout implements View {
    private static final Logger LOGGER = Logger.getLogger(HotelView.class.getName());

    public static final String VIEW_NAME = "hotel";

    @Autowired
    private MainViewDisplay mainViewDisplay;

    private IHotelService hotelService;
    private ICategoryService categoryService;

    private FilterWithClearBtn filterByName;
    private FilterWithClearBtn filterByAddress;
    @Getter
    final Button addHotelBtn = new Button("Add hotel");
    final Button deleteHotelBtn = new Button("Delete hotel");
    final Button editHotelBtn = new Button("Edit hotel");

    @Getter
    final Grid<Hotel> hotelList = new Grid<>();
    @Getter
    private HotelEditForm hotelEditForm = new HotelEditForm(this);

    public HotelView() {
        super();

        // del after debug
        System.out.println("start -> HotelView.CONSTRUCTOR ");

        // take beans ICategoryService & IHotelService
        this.hotelService = GetBeenFromSpringContext.getBeen(IHotelService.class);
        this.categoryService = GetBeenFromSpringContext.getBeen(ICategoryService.class);

        // del after debug
        System.out.println("getBeen(IHotelService.class): " + hotelService);
        System.out.println("STOP -> HotelView.CONSTRUCTOR ");
    }

    @PostConstruct
    void init() {
        // use this method to determine view if Spring will create been
        // if we created bean with our own hands & @PostConstruct don't work

        // del after debug
        System.out.println("start -> HotelView.init() ");

        // set view Configuration
        configureComponents();
        buildLayout();

        // del after debug
        System.out.println("STOP -> HotelView.init() ");
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // This view is constructed in the init() method()

        // del after debug
        System.out.println("start -> HotelView.enter() ");

        // set page URI in browser history
        NavigationUI.startPage.pushState(VIEW_NAME);

        mainViewDisplay.showView(this);

        // del after debug
        System.out.println("STOP -> HotelView.enter()");
    }

    private void configureComponents() {
        // filter fields with clear button
        filterByName = new FilterWithClearBtn("Filter by name...",
                e -> updateHotelList());
        filterByAddress = new FilterWithClearBtn("Filter by address...",
                e -> updateHotelList());

        // add Hotel Button
        addHotelBtn.setStyleName(ValoTheme.BUTTON_SMALL);
        addHotelBtn.addClickListener(e -> {
            addHotelBtn.setEnabled(false);
            hotelEditForm.setHotel(new Hotel());
        });

        // delete Hotel Button
        deleteHotelBtn.setStyleName(ValoTheme.BUTTON_DANGER);
        deleteHotelBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        deleteHotelBtn.setEnabled(false);
        deleteHotelBtn.addClickListener(e -> {
            int deletedHotelsCount = deleteSelectedHotels(hotelList.getSelectedItems());

            deleteHotelBtn.setEnabled(false);
            addHotelBtn.setEnabled(true);
            updateHotelList();
            Notification.show(String.format("Were deleted [%d] hotels.", deletedHotelsCount),
                    Notification.Type.WARNING_MESSAGE);
        });

        // edit Hotel Button (can edit only if one hotel was chosen)
        editHotelBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        editHotelBtn.setEnabled(false);
        editHotelBtn.addClickListener(e -> {
            addHotelBtn.setEnabled(true);       // switch on addNewHotel possibility
            Hotel editCandidate = hotelList.getSelectedItems().iterator().next();
            hotelEditForm.setHotel(editCandidate);
        });

        // Hotel list (Grid)
        hotelList.addColumn(Hotel::getName).setCaption("Name");
        hotelList.setFrozenColumnCount(1);              // froze "name" column
        hotelList.addColumn(Hotel::getAddress).setCaption("Address").setHidable(true);
        hotelList.addColumn(Hotel::getRating).setCaption("Rating").setHidable(true);
        hotelList.addColumn(hotel -> LocalDate.ofEpochDay(hotel.getOperatesFrom()))
                 .setCaption("Operates from").setHidable(true);;
        hotelList.addColumn(hotel -> {
            String categoryName = hotel.getCategory() != null
                    ? hotel.getCategory().getName()
                    : "";
            return this.existWithName(categoryName);
        }
        ).setCaption("Category").setHidable(true);;

        Grid.Column<Hotel, String> htmlColumn = hotelList.addColumn(hotel ->
                        "<a href='" + hotel.getUrl() + "' target='_blank'>more info</a>",
                new HtmlRenderer()).setCaption("Url").setHidable(true);;
        hotelList.addColumn(Hotel::getDescription).setCaption("Description").setHidable(true);;

        hotelList.setSelectionMode(Grid.SelectionMode.MULTI);           // multi select possible
        // delete and edit selected Hotel
        hotelList.addSelectionListener(e -> {
            // when Hotel is chosen - can delete or edit
            Set<Hotel> selectedHotels = e.getAllSelectedItems();
            if (selectedHotels != null && selectedHotels.size() == 1) {
                // chosen only one hotel - can add & delete & edit
                addHotelBtn.setEnabled(true);
                deleteHotelBtn.setEnabled(true);
                editHotelBtn.setEnabled(true);
            } else if (selectedHotels != null && selectedHotels.size() > 1) {
                // chosen more then one hotel - can delete & add
                hotelEditForm.setVisible(false);
                addHotelBtn.setEnabled(true);
                deleteHotelBtn.setEnabled(true);
                editHotelBtn.setEnabled(false);
            } else {
                // no any hotel chosen - can't delete & edit
                deleteHotelBtn.setEnabled(false);
                editHotelBtn.setEnabled(false);
                hotelEditForm.setVisible(false);
            }
        });

        this.updateHotelList();
    }

    private void buildLayout() {
        // tools bar - filters & buttons
        HorizontalLayout control = new HorizontalLayout(filterByName, filterByAddress,
                addHotelBtn, deleteHotelBtn, editHotelBtn);
        control.setMargin(false);
        control.setWidth("100%");
        // divide free space between filterByName (50%) & filterByAddress (50%)
        control.setExpandRatio(filterByName, 1);
        control.setExpandRatio(filterByAddress, 1);

        // content - HotelList & hotelEditForm
        HorizontalLayout hotelContent = new HorizontalLayout(hotelList, hotelEditForm);
        hotelList.setSizeFull();            // size 100% x 100%
        hotelList.addStyleName(ValoTheme.TABLE_SMALL);
        hotelEditForm.setSizeFull();
        hotelContent.setMargin(false);
        hotelContent.setWidth("100%");
        hotelContent.setHeight(31, Unit.REM);
        hotelContent.setExpandRatio(hotelList, 229);
        hotelContent.setExpandRatio(hotelEditForm, 92);

        // Compound view parts and allow resizing
        this.addComponents(control, hotelContent);
        this.setSpacing(true);
        this.setMargin(false);
        this.setWidth("100%");
    }

    public void updateHotelList() {
        try {
            List<Hotel> hotelList = hotelService.getAllByFilter(filterByName.getValue(),
                    filterByAddress.getValue());
            this.hotelList.setItems(hotelList);
            this.hotelEditForm.updateCategoryItems();
        } catch (Exception exp) {
            LOGGER.log(Level.WARNING,
                    String.format("Can't take from DB all hotels by filters: name contains [%s] & address contains [%s]",
                            filterByName.getValue(), filterByAddress.getValue()), exp);
        }

    }

    private int deleteSelectedHotels(Set<Hotel> selectedHotels) {
        int count = 0;
        Hotel hotelForeDelete = null;
        try {
            for (Hotel hotel : selectedHotels) {
                hotelService.delete(hotel.getId());
                count++;
            }
        } catch (Exception exp) {
            LOGGER.log(Level.WARNING, "Can't delete from DB hotel: " + hotelForeDelete, exp);
            Notification.show("Connection with DB was lost while deleting.", Notification.Type.ERROR_MESSAGE);
        }

        return count;
    }

    private String existWithName(String categoryName) {
        try {
            return categoryService.existWithName(categoryName)
                    ? categoryName
                    : Category.NULL_CATEGORY_REPRESENTATION;
        } catch (Exception exp) {
            LOGGER.log(Level.WARNING, "Can't take from DB category by name: " + categoryName, exp);

            return Category.NULL_CATEGORY_REPRESENTATION;
        }


    }
}
