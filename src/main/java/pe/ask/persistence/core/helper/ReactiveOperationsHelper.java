package pe.ask.persistence.core.helper;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pe.ask.persistence.core.mapper.GenericBeanMapper;
import pe.ask.persistence.core.models.PageResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.beans.FeatureDescriptor;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class ReactiveOperationsHelper <D, E, I, R extends ReactiveCrudRepository<E, I> & ReactiveQueryByExampleExecutor<E>> {
    protected R repository;
    private final GenericBeanMapper<D, E> mapper;
    protected String defaultSortField = "id";

    @SuppressWarnings("unchecked")
    protected ReactiveOperationsHelper(R repository) {
        this.repository = repository;
        Class<?>[] typeArguments = GenericTypeResolver.resolveTypeArguments(getClass(), ReactiveOperationsHelper.class);
        Class<D> domainClass = (Class<D>) Objects.requireNonNull(typeArguments)[0];
        Class<E> entityClass = (Class<E>) Objects.requireNonNull(typeArguments)[1];
        this.mapper = new GenericBeanMapper<>(domainClass, entityClass);
    }

    protected R getRepository() {
        return this.repository;
    }

    protected E toEntity(D domain) {
        return mapper.toEntity(domain);
    }

    protected D toDomain(E domain) {
        return mapper.toDomain(domain);
    }

    public Mono<D> save(D domain) {
        return Mono.justOrEmpty(domain)
                .map(this::toEntity)
                .map(this::beforeSave)
                .flatMap(repository::save)
                .map(this::toDomain);
    }

    public Flux<D> saveAll(Flux<D> domains) {
        return repository.saveAll(domains.map(this::toEntity))
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

    public Flux<D> findAll() {
        return repository.findAll()
                .map(this::toDomain);
    }

    public Mono<PageResponse<D>> findPaginated(D domain, int page, int size, String sort, String direction) {
        org.springframework.data.domain.PageRequest springPageRequest = org.springframework.data.domain.PageRequest.of(
                page,
                size,
                Sort.by(
                        Optional.ofNullable(direction)
                                .filter("asc"::equalsIgnoreCase)
                                .map(d -> Sort.Direction.ASC)
                                .orElse(Sort.Direction.DESC),
                        Optional.ofNullable(sort)
                                .filter(s -> !s.trim().isEmpty())
                                .orElse(defaultSortField)
                )
        );

        return Mono.fromCallable(() -> {
                    if (domain == null) {
                        return this.mapper.toEntity(null);
                    }
                    return this.toEntity(domain);
                })
                .map(Example::of)
                .flatMap(example -> repository.findBy(example, query -> query.page(springPageRequest)))
                .map(springPage -> new PageResponse<>(
                        springPage.getContent().stream().map(this::toDomain).toList(),
                        springPage.getNumber(),
                        springPage.getSize(),
                        springPage.getTotalElements(),
                        springPage.getTotalPages(),
                        springPage.isLast()
                ));
    }

    public Mono<PageResponse<D>> findPaginated(D domain, int page, int size) {
        return this.findPaginated(domain, page, size, null, null);
    }

    public Mono<PageResponse<D>> findPaginated(int page, int size) {
        return this.findPaginated(null, page, size, null, null);
    }

    public Mono<D> patch(I id, D partialDomain) {
        return repository.findById(id)
                .map(existingEntity -> {
                    E incomingEntity = this.toEntity(partialDomain);
                    copyNonNullProperties(incomingEntity, existingEntity);
                    return existingEntity;
                })
                .flatMap(repository::save)
                .map(this::toDomain);
    }

    public Mono<Long> countByExample(D domain) {
        return Mono.justOrEmpty(domain)
                .map(this::toEntity)
                .map(Example::of)
                .flatMap(repository::count);
    }

    public Mono<Boolean> existsByExample(D domain) {
        return Mono.justOrEmpty(domain)
                .map(this::toEntity)
                .map(Example::of)
                .flatMap(repository::exists);
    }

    protected E beforeSave(E entity) {
        return entity;
    }

    protected void copyNonNullProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);

        return Stream.of(wrappedSource.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) == null)
                .toArray(String[]::new);
    }
}
