Here is the unit function for Callable (we will refer to it as unit in the text to avoid ambiguity):

public static <A> Callable<A> callable(final A a) {
return new Callable<A>() {
public A call() {
return a;
}
};
}

And here is its binding function:

public static <A, B> Callable<B> bind(final Callable<A> a, final F<A, Callable<B>> f) {
return new Callable<B>() {
public B call() throws Exception {
return f.f(a.call()).call();
}
};
}

This method is called fmap, and we can define it in terms of bind:

public static <A, B> F<Callable<A>, Callable<B>> fmap(final F<A, B> f) {
return new F<Callable<A>, Callable<B>>() {
public Callable<B> f(final Callable<A> a) {
return bind(a, new F<A, Callable<B>>() {
public Callable<B> f(final A ab) {
return new Callable<B>() {
public B call() {
return f.f(ab);
}
};
}
});
}
};
}


We will use a new class, Strategy<A>, that allows us to effectively separate parallelism from the algorithm itself:

public final class Strategy<A> {
    
    private F<Callable<A>, Future<A>> f;
    
    private Strategy(final F<Callable<A>, Future<A>> f) {
        this.f = f;
    }
    
    public F<Callable<A>, Future<A>> f() {
        return f;
    }
    
    public static <A> Strategy<A> strategy(final F<Callable<A>, Future<A>> f) {
        return new Strategy<A>(f);
    }
    
}

We’ll add a couple of static functions to create simple strategies:

public static <A> Strategy<A> simpleThreadStrategy() {
return strategy(new F<Callable<A>, Future<A>>() {
public Future<A> f(final Callable<A> p) {
final FutureTask<A> t = new FutureTask<A>(p);
new Thread(t).start();
return t;
}
});
}

public static <A> Strategy<A> executorStrategy(final ExecutorService s) {
return strategy(new F<Callable<A>, Future<A>>() {
public Future<A> f(final Callable<A> p) {
return s.submit(p);
}
});
}

One of the neat things that working with Strategies as functions allows us to do is use the Callable monad to compose them with existing functions. Any function can be lifted into the Callable monad using fmap, and then composed with a Strategy to yield a concurrent function. Moreover, we can use Strategies to convert existing functions to concurrent functions. The following method on Strategy will take any function and return the equivalent function that executes concurrently. Calling such a function will give you a Future value from which you can get the computed result whenever it’s ready.

public <B> F<B, Future<A>> lift(final F<B, A> f) {
final Strategy<A> self = this;
return new F<B, Future<A>>() {
public Future<A> f(final B b) {
return self.f().f(new Callable<A>() {
public A call() {
return f.f(b);
}
});
}
};
}




In fact, we might want to wrap values of type Future<A> inside of a Callable<A> again so that we can manipulate their return values while they are running:

public static <A> Callable<A> obtain(final Future<A> x) {
return new Callable<A>() {
public A call() throws Exception {
return x.get();
}
};
}

And this takes us full circle back into the Callable monad, where we can compose computations, bind them to functions and map functions over them, all lazily. Which means: without actually asking what their values are until we absolutely need to know.



