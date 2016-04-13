package jk.dev.cryptomessaging.Utilities;

import java.util.Comparator;

/**
 * Created by Jim on 13/4/2016.
 */
public class CategorySorter implements Comparator<Image> {
    @Override
    public int compare(Image one, Image another) {
        return one.getCategory().compareTo(another.getCategory());
    }
}
