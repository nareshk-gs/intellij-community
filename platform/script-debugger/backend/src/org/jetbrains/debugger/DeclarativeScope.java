package org.jetbrains.debugger;

import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.AsyncValueLoaderManager;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.debugger.values.ObjectValue;
import org.jetbrains.debugger.values.ValueManager;

import java.util.List;

public abstract class DeclarativeScope<VALUE_LOADER extends ValueManager> extends ScopeBase {
  private static final AsyncValueLoaderManager<DeclarativeScope, List<Variable>> VARIABLES_LOADER =
    new AsyncValueLoaderManager<DeclarativeScope, List<Variable>>(DeclarativeScope.class) {
      @Override
      public boolean isUpToDate(@NotNull DeclarativeScope host, @NotNull List<Variable> data) {
        return host.valueManager.getCacheStamp() == host.cacheStamp;
      }

      @Override
      public void load(@NotNull DeclarativeScope host, @NotNull AsyncResult<List<Variable>> result) {
        host.loadVariables(result);
      }
    };

  @SuppressWarnings("UnusedDeclaration")
  private volatile AsyncResult<List<? extends Variable>> variables;

  private volatile int cacheStamp = -1;

  protected final VALUE_LOADER valueManager;

  protected DeclarativeScope(@NotNull Type type, @Nullable String description, @NotNull VALUE_LOADER valueManager) {
    super(type, description);

    this.valueManager = valueManager;
  }

  /**
   * You must call {@link #updateCacheStamp()} when data loaded
   */
  protected abstract void loadVariables(@NotNull AsyncResult<List<? extends Variable>> result);

  protected final void updateCacheStamp() {
    cacheStamp = valueManager.getCacheStamp();
  }

  protected final void loadScopeObjectProperties(@NotNull ObjectValue value, @NotNull final AsyncResult<List<? extends Variable>> result) {
    if (valueManager.rejectIfObsolete(result)) {
      return;
    }

    value.getProperties().done(new Consumer<List<Variable>>() {
      @Override
      public void consume(List<Variable> variables) {
        updateCacheStamp();
        result.setDone(variables);
      }
    }).rejected(new Consumer<String>() {
      @Override
      public void consume(String error) {
        result.reject(error);
      }
    });
  }

  @NotNull
  @Override
  public final Promise<List<Variable>> getVariables() {
    return Promise.wrap(VARIABLES_LOADER.get(this));
  }

  @NotNull
  @Override
  public Promise<Void> clearCaches() {
    cacheStamp = -1;
    VARIABLES_LOADER.reset(this);
    return Promise.DONE;
  }
}