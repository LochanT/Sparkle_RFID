package com.loyalstring.rfid.data.reader;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class RFIDReaderManager_Factory implements Factory<RFIDReaderManager> {
  private final Provider<Context> contextProvider;

  public RFIDReaderManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public RFIDReaderManager get() {
    return newInstance(contextProvider.get());
  }

  public static RFIDReaderManager_Factory create(Provider<Context> contextProvider) {
    return new RFIDReaderManager_Factory(contextProvider);
  }

  public static RFIDReaderManager newInstance(Context context) {
    return new RFIDReaderManager(context);
  }
}
