package com.loyalstring.rfid.di;

import android.content.Context;
import com.loyalstring.rfid.data.reader.RFIDReaderManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class ReaderModule_ProvideReaderManagerFactory implements Factory<RFIDReaderManager> {
  private final Provider<Context> contextProvider;

  public ReaderModule_ProvideReaderManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public RFIDReaderManager get() {
    return provideReaderManager(contextProvider.get());
  }

  public static ReaderModule_ProvideReaderManagerFactory create(Provider<Context> contextProvider) {
    return new ReaderModule_ProvideReaderManagerFactory(contextProvider);
  }

  public static RFIDReaderManager provideReaderManager(Context context) {
    return Preconditions.checkNotNullFromProvides(ReaderModule.INSTANCE.provideReaderManager(context));
  }
}
