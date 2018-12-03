from django.urls import path

from . import views

urlpatterns = [
    path('', views.enterConfig, name='config'),
    path('plots', views.viewPlots, name='plots'),
]
