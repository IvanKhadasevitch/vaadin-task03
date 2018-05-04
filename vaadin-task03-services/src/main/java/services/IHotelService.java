package services;

import entities.Hotel;

import java.util.List;

public interface IHotelService extends IService<Hotel> {
    /**
     * save or update in DB hotel or return null if impossible
     *
     * @param hotel entity for save or update
     * @return saved or updated in DB hotel or
     *         return null if impossible
     */
    Hotel save(Hotel hotel);

    List<Hotel> getAllByFilter(String nameFilter, String addressFilter);
}
