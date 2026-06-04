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

public abstract class ReactiveAdapterOperation<D, E, I, R extends ReactiveCrudRepository<E, I> & ReactiveQueryByExampleExecutor<E>> {
    protected R repository;
    protected ObjectMapper mapper;
    private final Class<E>  entityClass;
    private final Function<E, D> toDomainFn;

    @SuppressWarnings("unchecked")
    protected ReactiveAdapterOperation(R repository, ObjectMapper mapper, Function<E, D> toDomainFn) {
        this.repository = repository;
        this.mapper = mapper;
        Class<?>[] typeArguments = GenericTypeResolver.resolveTypeArguments(getClass(), ReactiveAdapterOperation.class);
        this.entityClass = (Class<E>) Objects.requireNonNull(typeArguments)[1];
        this.toDomainFn = toDomainFn;
    }

    protected E toEntity(D domain) {
        return domain != null ? mapper.convertValue(domain, entityClass) : null;
    }

    protected D toDomain(E entity) {
        return entity != null ? toDomainFn.apply(entity) : null;
    }

    public Mono<D> save(D domain) {
        return Mono.justOrEmpty(domain)
                .map(this::toEntity)
                .flatMap(repository::save)
                .map(this::toDomain);
    }

    public Flux<D> saveAll(Flux<D> domains) {
        return domains.flatMap(domain -> Mono.justOrEmpty(domain).map(this::toEntity))
                .transform(repository::saveAll)
                .map(this::toDomain);
    }

    public Mono<D> findById(I id) {
        return repository.findById(id)
                .map(this::toDomain);
    }

    public Mono<Boolean> existsById(I id) {
        return repository.existsById(id);
    }

    public Mono<Long> count() {
        return repository.count();
    }

    public Flux<D> findByExample(D domain) {
        return Mono.justOrEmpty(domain)
                .map(this::toEntity)
                .map(Example::of)
                .flatMapMany(repository::findAll)
                .map(this::toDomain);
    }

    public Mono<Page<D>> findPaginated(D domain, Pageable pageable) {
        return Mono.justOrEmpty(domain)
                .map(this::toEntity)
                .map(Example::of)
                .flatMap(example -> repository.findBy(example, query -> query.page(pageable)))
                .map(page -> page.map(this::toDomain));
    }

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

    public Flux<D> findAll() {
        return repository.findAll()
                .map(this::toDomain);
    }

    public Mono<Void> deleteById(I id) {
        return repository.deleteById(id);
    }

    public Mono<Void> delete(D domain) {
        return Mono.justOrEmpty(domain)
                .map(this::toEntity)
                .flatMap(repository::delete);
    }
}
