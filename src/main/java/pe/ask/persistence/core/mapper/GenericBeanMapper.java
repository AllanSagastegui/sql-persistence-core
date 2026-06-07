package pe.ask.persistence.core.mapper;

import org.springframework.beans.BeanUtils;
import pe.ask.persistence.core.exception.MapFailedException;

public class GenericBeanMapper<D, E> {

    private final Class<D> domainClass;
    private final Class<E> entityClass;

    public GenericBeanMapper(Class<D> domainClass, Class<E> entityClass) {
        this.domainClass = domainClass;
        this.entityClass = entityClass;
    }

    public E toEntity(D domain) {
        try {
            E entity = entityClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(domain, entity);
            return entity;
        } catch (Exception e) {
            throw new MapFailedException();
        }
    }

    public D toDomain(E entity) {
        try {
            D domain = domainClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(entity, domain);
            return domain;
        } catch (Exception e) {
            throw new MapFailedException();
        }
    }
}
