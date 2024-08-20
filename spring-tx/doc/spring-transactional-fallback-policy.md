# @Transactional Fallback Policy

어노테이션 기반의 대체 정책(fallback policy)은 다음과 같다.

1. 타깃 메서드
2. 타깃 클래스
3. 인터페이스 메서드
4. 인터페이스

## @Transactional Target

@Transactional 어노테이션은 인터페이스, 인터페이스의 메서드, 클래스 또는 클래스의 메서드에 선언할 수 있다.

> 만약 모든 메소드에 @Transactional 이 선언되어 있다면, 메소드가 상당히 더러워진다. 스프링은 클래스와 인터페이스에 어노테이션을 선언할 수 있도록 지원함으로써, 메서드마다 불필요하게 선언된 중복된 트랜잭션 어노테이션을 방지할 수 있도록 지원한다.

```java
package org.springframework.transaction.annotation;

@Target({ElementType.TYPE, ElementType.METHOD}) // here
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Reflective
public @interface Transactional {
    //...
}
```

단 Spring 에선 AspectJ 를 고려해서, 클래스와 클래스 메서드에 대해서만 선언할 수 있도록 권장한다.

> Java 어노테이션은 인터페이스에서 상속되지 않음으로 AspectJ 모드를 사용할 때 위빙이 되지 않는 이슈가 존재한다. <br/>
> The Spring team recommends that you annotate methods of concrete classes with the @Transactional annotation, rather than relying on annotated methods in interfaces, even if the latter does work for interface-based and target-class proxies as of 5.0. Since Java annotations are not inherited from interfaces, interface-declared annotations are still not recognized by the weaving infrastructure when using AspectJ mode, so the aspect does not get applied. As a consequence, your transaction annotations may be silently ignored: Your code might appear to "work" until you test a rollback scenario.

## Method-level vs Class-level Transactional

다음과 같이 클래스와 메서드에 @Transactional 선언되어 있다.

```java
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class MemberService {

    @Transactional
    public void save(Member member) {}
}
```

save 메서드를 호출하게 되면 저장이 될까? 저장 된다. 

[Spring @Transactional 문서](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html)에 보면, 메서드의 트랜잭션 설정은 구조상 가까운 @Transactional 메타데이터를 참조한다. 클래스에 선언되어 있더라도, 메서드에 선언된 @Transactional 더 우선순위가 높다. 

```text
The most derived location takes precedence when evaluating the transactional settings for a method. In the case of the following example, the DefaultFooService class is annotated at the class level with the settings for a read-only transaction, but the @Transactional annotation on the updateFoo(Foo) method in the same class takes precedence over the transactional settings defined at the class level.

```

스프링은 @Transactional 선언된 위치에 따라 우선 순위를 정하게 되는데 이를 대체 정책(fallback policy)이라 한다.

### AnnotationTransactionAttributeSource

스프링의 트랜잭션의 대체 정책을 살펴보려면, 트랜잭션이 어디서 주입하고 있는지 살펴봐야 된다.

> Spring @Transactional 은 TransactionInterceptor 을 통해 주입된다.

- TransactionInterceptor#invoke
  - TransactionAspectSupport#invokeWithinTransaction

TransactionAspectSupport 소스를 보면, 호출된 메서드와 대상 클래스 정보를 활용해 트랜잭션 메타데이터를 관리하는 TransactionAttribute 객체를 얻는 과정을 확인할 수 있다.

```java
package org.springframework.transaction.interceptor;

public abstract class TransactionAspectSupport implements BeanFactoryAware, InitializingBean {
        
    protected Object invokeWithinTransaction(Method method, @Nullable Class<?> targetClass,
            final InvocationCallback invocation) throws Throwable {
    
        // If the transaction attribute is null, the method is non-transactional.
        TransactionAttributeSource tas = getTransactionAttributeSource();
        
        // here!!!
        final TransactionAttribute txAttr = (tas != null ? tas.getTransactionAttribute(method, targetClass) : null);
        
        // ...
    }
}
```

TransactionAttribute 엔 크게 어노테이션 기반과 메서드명 기반으로 나뉜다. 

- AnnotationTransactionAttributeSource: 어노테이션 기반
- NameMatchTransactionAttributeSource: 메서드명 기반

## Fallback Policy

- AbstractFallbackTransactionAttributeSource
  - getTransactionAttribute
    - `computeTransactionAttribute`

어노테이션 기반의 대체 정책은 다음과 같다.

1. 타깃 메서드
2. 타깃 클래스
3. 인터페이스 메서드
4. 인터페이스

```java
public class AnnotationTransactionAttributeSource 
        extends AbstractFallbackTransactionAttributeSource
        implements Serializable 
{
    // ...
}

public abstract class AbstractFallbackTransactionAttributeSource
        implements TransactionAttributeSource, EmbeddedValueResolverAware {

    @Override
    @Nullable
    public TransactionAttribute getTransactionAttribute(Method method, @Nullable Class<?> targetClass) {
        if (method.getDeclaringClass() == Object.class) {
            return null;
        }

        Object cacheKey = getCacheKey(method, targetClass);
        TransactionAttribute cached = this.attributeCache.get(cacheKey);

        if (cached != null) {
            return (cached != NULL_TRANSACTION_ATTRIBUTE ? cached : null);
        }
        
        else {
            // here
            TransactionAttribute txAttr = computeTransactionAttribute(method, targetClass);
            
            // ...
            return txAttr;
        }
    }

    
    @Nullable
	protected TransactionAttribute computeTransactionAttribute(Method method, @Nullable Class<?> targetClass) {
		// Don't allow non-public methods, as configured.
		if (allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers())) {
			return null;
		}

		// The method may be on an interface, but we need attributes from the target class.
		// If the target class is null, the method will be unchanged.
		Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

		// First try is the method in the target class.
		TransactionAttribute txAttr = findTransactionAttribute(specificMethod);
		if (txAttr != null) {
			return txAttr;
		}

		// Second try is the transaction attribute on the target class.
		txAttr = findTransactionAttribute(specificMethod.getDeclaringClass());
		if (txAttr != null && ClassUtils.isUserLevelMethod(method)) {
			return txAttr;
		}

		if (specificMethod != method) {
			// Fallback is to look at the original method.
			txAttr = findTransactionAttribute(method);
			if (txAttr != null) {
				return txAttr;
			}
			// Last fallback is the class of the original method.
			txAttr = findTransactionAttribute(method.getDeclaringClass());
			if (txAttr != null && ClassUtils.isUserLevelMethod(method)) {
				return txAttr;
			}
		}

		return null;
	}
}
```

## 마무리

다음 예제 코드에서 트랜잭션 대체 정책은 다음과 같다.

5, 6 `->` 4 `->` 2, 3 `->` 1

```java
import org.springframework.transaction.annotation.Transactional;

/**
 * @see <a href="https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html">Using @Transactional</a> 
 * */
@Transactional(readOnly = true) // 1
public interface FooService {
    @Transactional(readOnly = true) Foo get(String fooName); // 2
    @Transactional void save(Foo foo); // 3
}

@Transactional(readOnly = true) // 4
public class DefaultFooService implements FooService {

    @Transactional(readOnly = true) // 5
    @Override
    public Foo get(String fooName) { }

    @Transactional // 6
    @Override
    public void save(Foo foo) { }
}
```

## Reference

- [docs.spring.io - transaction](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html)
  - [Using @Transactional](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html)
