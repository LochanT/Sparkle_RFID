package com.loyalstring.rfid.viewmodel;

import com.loyalstring.rfid.data.reader.RFIDReaderManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
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
public final class BulkViewModel_Factory implements Factory<BulkViewModel> {
  private final Provider<RFIDReaderManager> readerManagerProvider;

  public BulkViewModel_Factory(Provider<RFIDReaderManager> readerManagerProvider) {
    this.readerManagerProvider = readerManagerProvider;
  }

  @Override
  public BulkViewModel get() {
    return newInstance(readerManagerProvider.get());
  }

  public static BulkViewModel_Factory create(Provider<RFIDReaderManager> readerManagerProvider) {
    return new BulkViewModel_Factory(readerManagerProvider);
  }

  public static BulkViewModel newInstance(RFIDReaderManager readerManager) {
    return new BulkViewModel(readerManager);
  }
}
