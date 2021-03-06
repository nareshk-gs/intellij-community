// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.openapi.vcs;

import com.intellij.notification.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.changes.ui.ChangesViewContentManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VcsNotifier {

  public static final NotificationGroup NOTIFICATION_GROUP_ID = NotificationGroup.toolWindowGroup(
    "Vcs Messages", ChangesViewContentManager.TOOLWINDOW_ID);
  public static final NotificationGroup IMPORTANT_ERROR_NOTIFICATION = new NotificationGroup(
    "Vcs Important Messages", NotificationDisplayType.STICKY_BALLOON, true);
  public static final NotificationGroup STANDARD_NOTIFICATION = new NotificationGroup(
    "Vcs Notifications", NotificationDisplayType.BALLOON, true);
  public static final NotificationGroup SILENT_NOTIFICATION = new NotificationGroup(
    "Vcs Silent Notifications", NotificationDisplayType.NONE, true);

  private final @NotNull Project myProject;


  public static VcsNotifier getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, VcsNotifier.class);
  }

  public VcsNotifier(@NotNull Project project) {
    myProject = project;
  }

  @NotNull
  public static Notification createNotification(
    @NotNull NotificationGroup notificationGroup,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message,
    @NotNull NotificationType type,
    @Nullable NotificationListener listener
  ) {
    // title can be empty; message can't be neither null, nor empty
    if (StringUtil.isEmptyOrSpaces(message)) {
      message = title;
      title = "";
    }
    // if both title and message were empty, then it is a problem in the calling code => Notifications engine assertion will notify.
    return notificationGroup.createNotification(title, message, type, listener);
  }

  @NotNull
  public Notification notify(
    @NotNull NotificationGroup notificationGroup,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message,
    @NotNull NotificationType type,
    @Nullable NotificationListener listener
  ) {
    Notification notification = createNotification(notificationGroup, title, message, type, listener);
    return notify(notification);
  }

  @NotNull
  public Notification notify(
    @NotNull NotificationGroup notificationGroup,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message,
    @NotNull NotificationType type,
    NotificationAction... actions
  ) {
    Notification notification = createNotification(notificationGroup, title, message, type, null);
    for (NotificationAction action : actions) {
      notification.addAction(action);
    }
    return notify(notification);
  }

  @NotNull
  public Notification notify(@NotNull Notification notification) {
    notification.notify(myProject);
    return notification;
  }

  @NotNull
  public Notification notifyError(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message
  ) {
    return notifyError(title, message, (NotificationListener)null);
  }

  @NotNull
  public Notification notifyError(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message,
    @Nullable NotificationListener listener
  ) {
    return notify(IMPORTANT_ERROR_NOTIFICATION, title, message, NotificationType.ERROR, listener);
  }

  @NotNull
  public Notification notifyError(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message,
    NotificationAction... actions
  ) {
    return notify(IMPORTANT_ERROR_NOTIFICATION, title, message, NotificationType.ERROR, actions);
  }

  @NotNull
  public Notification notifyWeakError(@Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message) {
    return notifyWeakError("", message);
  }

  @NotNull
  public Notification notifyWeakError(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message
  ) {
    return notify(NOTIFICATION_GROUP_ID, title, message, NotificationType.ERROR);
  }

  @NotNull
  public Notification notifySuccess(@Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message) {
    return notifySuccess("", message);
  }

  @NotNull
  public Notification notifySuccess(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message
  ) {
    return notifySuccess(title, message, null);
  }

  @NotNull
  public Notification notifySuccess(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message,
    @Nullable NotificationListener listener
  ) {
    return notify(NOTIFICATION_GROUP_ID, title, message, NotificationType.INFORMATION, listener);
  }

  @NotNull
  public Notification notifyImportantInfo(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message
  ) {
    return notify(IMPORTANT_ERROR_NOTIFICATION, title, message, NotificationType.INFORMATION);
  }

  @NotNull
  public Notification notifyImportantInfo(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message,
    @Nullable NotificationListener listener
  ) {
    return notify(IMPORTANT_ERROR_NOTIFICATION, title, message, NotificationType.INFORMATION, listener);
  }

  @NotNull
  public Notification notifyInfo(@Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message) {
    return notifyInfo("", message);
  }

  @NotNull
  public Notification notifyInfo(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message
  ) {
    return notifyInfo(title, message, null);
  }

  @NotNull
  public Notification notifyInfo(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message,
    @Nullable NotificationListener listener
  ) {
    return notify(NOTIFICATION_GROUP_ID, title, message, NotificationType.INFORMATION, listener);
  }

  @NotNull
  public Notification notifyMinorWarning(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message
  ) {
    return notifyMinorWarning(title, message, null);
  }

  @NotNull
  public Notification notifyMinorWarning(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message,
    @Nullable NotificationListener listener
  ) {
    return notify(STANDARD_NOTIFICATION, title, message, NotificationType.WARNING, listener);
  }

  @NotNull
  public Notification notifyWarning(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message
  ) {
    return notifyWarning(title, message, null);
  }

  @NotNull
  public Notification notifyWarning(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message,
    @Nullable NotificationListener listener
  ) {
    return notify(NOTIFICATION_GROUP_ID, title, message, NotificationType.WARNING, listener);
  }

  @NotNull
  public Notification notifyImportantWarning(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message
  ) {
    return notify(IMPORTANT_ERROR_NOTIFICATION, title, message, NotificationType.WARNING);
  }

  @NotNull
  public Notification notifyImportantWarning(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message,
    @Nullable NotificationListener listener
  ) {
    return notify(IMPORTANT_ERROR_NOTIFICATION, title, message, NotificationType.WARNING, listener);
  }

  @NotNull
  public Notification notifyMinorInfo(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message
  ) {
    return notifyMinorInfo(title, message, (NotificationListener)null);
  }

  @NotNull
  public Notification notifyMinorInfo(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message,
    @Nullable NotificationListener listener
  ) {
    return notify(STANDARD_NOTIFICATION, title, message, NotificationType.INFORMATION, listener);
  }

  @NotNull
  public Notification notifyMinorInfo(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message,
    NotificationAction... actions
  ) {
    return notify(STANDARD_NOTIFICATION, title, message, NotificationType.INFORMATION, actions);
  }

  @NotNull
  public Notification notifyMinorInfo(
    boolean sticky,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message,
    NotificationAction... actions
  ) {
    return notify(sticky ? IMPORTANT_ERROR_NOTIFICATION : STANDARD_NOTIFICATION, title, message, NotificationType.INFORMATION, actions);
  }

  public Notification logInfo(
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
    @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String message
  ) {
    return notify(SILENT_NOTIFICATION, title, message, NotificationType.INFORMATION);
  }

  public void showNotificationAndHideExisting(@NotNull Notification notificationToShow, @NotNull Class<? extends Notification> klass) {
    hideAllNotificationsByType(klass);
    notificationToShow.notify(myProject);
  }

  public void hideAllNotificationsByType(@NotNull Class<? extends Notification> klass) {
    NotificationsManager notificationsManager = NotificationsManager.getNotificationsManager();
    for (Notification notification : notificationsManager.getNotificationsOfType(klass, myProject)) {
      notification.expire();
    }
  }
}
