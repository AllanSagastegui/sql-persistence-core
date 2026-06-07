package pe.ask.persistence.core.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Deprecated abstract base class for reactive adapter operations.
 * <p>
 * Provided basic CRUD operations by mapping between domain models and database entities.
 * </p>
 *
 * @param <D> the domain model type
 * @param <E> the entity model type
 * @param <I> the identifier type
 * @param <R> the reactive repository type
 *
 * @author Allan Sagastegui
 * @deprecated since 1.0.4, use {@link ReactiveOperationsHelper} instead.
 */
@Deprecated(since = "1.0.4")
public abstract class ReactiveAdapterOperation<D, E, I, R extends ReactiveCrudRepository<E, I> & ReactiveQueryByExampleExecutor<E>> {

    /**
     * The repository used for database operations.
     */
    protected R repository;

    /**
     * The object mapper used for conversion.
     */
    protected ObjectMapper mapper;

    private final Class<E> entityClass;
    private final Function<E, D> toDomainFn;

    /**
     * Constructs a new ReactiveAdapterOperation.
     *
     * @param repository the repository instance
     * @param mapper the object mapper instance
     * @param toDomainFn the function to convert entity to domain model
     */
    @SuppressWarnings("unchecked")
    protected ReactiveAdapterOperation(R repository, ObjectMapper mapper, Function<E, D> toDomainFn) {
        this.repository = repository;
        this.mapper = mapper;
        Class<?>[] typeArguments = GenericTypeResolver.resolveTypeArguments(getClass(), ReactiveAdapterOperation.class);
        this.entityClass = (Class<E>) Objects.requireNonNull(typeArguments)[1];
        this.toDomainFn = toDomainFn;
    }

    /**
     * Converts a domain model to an entity.
     *
     * @param domain the domain model
     * @return the corresponding entity
     */
    protected E toEntity(D domain) {
        return domain != null ? mapper.convertValue(domain, entityClass) : null;
    }

    /**
     * Converts an entity to a domain model.
     *
     * @param entity the entity
     * @return the corresponding domain model
     */
    protected D toDomain(E entity) {
        return entity != null ? toDomainFn.apply(entity) : null;
    }

    /**
     * Saves a domain model.
     *
     * @param domain the domain model to save
     * @return a Mono emitting the saved domain model
     */
    public Mono<D> save(D domain) {
        return Mono.justOrEmpty(domain)
                .map(this::toEntity)
                .flatMap(repository::save)
                .map(this::toDomain);
    }

    /**
     * Saves multiple domain models.
     *
     * @param domains a Flux of domain models to save
     * @return a Flux emitting the saved domain models
     */
    public Flux<D> saveAll(Flux<D> domains) {
        return domains.flatMap(domain -> Mono.justOrEmpty(domain).map(this::toEntity))
                .transform(repository::saveAll)
                .map(this::toDomain);
    }

    /**
     * Finds a domain model by its identifier.
     *
     * @param id the identifier
     * @return a Mono emitting the domain model if found
     */
    public Mono<D> findById(I id) {
        return repository.findById(id)
                .map(this::toDomain);
    }

    /**
     * Checks if a domain model exists by its identifier.
     *
     * @param id the identifier
     * @return a Mono emitting true if it exists, false otherwise
     */
    public Mono<Boolean> existsById(I id) {
        return repository.existsById(id);
    }

    /**
     * Counts the total number of domain models.
     *
     * @return a Mono emitting the total count
     */
    public Mono<Long> count() {
        return repository.count();
    }

    /**
     * Finds domain models matching the given example.
     *
     * @param domain the example domain model
     * @return a Flux emitting the matching domain models
     */
    public Flux<D> findByExample(D domain) {
        return Mono.justOrEmpty(domain)
                .map(this::toEntity)
                .map(Example::of)
                .flatMapMany(repository::findAll)
                .map(this::toDomain);
    }

    /**
     * Finds a paginated list of domain models matching the given example.
     *
     * @param domain the example domain model
     * @param pageable the pagination information
     * @return a Mono emitting the paginated result
     */
    public Mono<Page<D>> findPaginated(D domain, Pageable pageable) {
        return Mono.justOrEmpty(domain)
                .map(this::toEntity)
                .map(Example::of)
                .flatMap(example -> repository.findBy(example, query -> query.page(pageable)))
                .map(page -> page.map(this::toDomain));
    }

    /**
     * Finds a paginated list of domain models matching the given example with sorting.
     *
     * @param domain the example domain model
     * @param page the page number
     * @param size the page size
     * @param sort the sort field
     * @param direction the sort direction
     * @return a Mono emitting the paginated result
     */
    public Mono<Page<D>> findPaginated(D domain, int page, int size, String sort, String direction) {
        return findPaginated(
                domain,
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Optional.ofNullable(direction)
                                        .filter("asc"::equalsIgnoreCase)
                                        .map(d -> Sort.Direction.ASC)
                                        .orElse(Sort.Direction.DESC),
                                Optional.ofNullable(sort)
                                        .filter(s -> !s.trim().isEmpty())
                                        .orElse("id")
                        )
                )
        );
    }

    /**
     * Finds all domain models.
     *
     * @return a Flux emitting all domain models
     */
    public Flux<D> findAll() {
        return repository.findAll()
                .map(this::toDomain);
    }

    /**
     * Deletes a domain model by its identifier.
     *
     * @param id the identifier
     * @return a Mono signaling completion
     */
    public Mono<Void> deleteById(I id) {
        return repository.deleteById(id);
    }

    /**
     * Deletes a given domain model.
     *
     * @param domain the domain model to delete
     * @return a Mono signaling completion
     */
    public Mono<Void> delete(D domain) {
        return Mono.justOrEmpty(domain)
                .map(this::toEntity)
                .flatMap(repository::delete);
    }
}