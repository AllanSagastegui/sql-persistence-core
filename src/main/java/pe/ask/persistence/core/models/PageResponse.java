package pe.ask.persistence.core.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a paginated response containing a list of items and pagination metadata.
 *
 * @param <T> the type of the content items in the page
 *
 * @author Allan Sagastegui
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /**
     * The list of items in the page.
     */
    private List<T> content;

    /**
     * The current page number.
     */
    private int pageNumber;

    /**
     * The size of the page.
     */
    private int pageSize;

    /**
     * Whether this is the last page.
     */
    private boolean isLast;

    /**
     * Has previous?.
     */
    private boolean hasPrevious;

    /**
     * Has next?.
     */
    private boolean hasNext;
}