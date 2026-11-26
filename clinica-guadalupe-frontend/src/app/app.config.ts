import { ApplicationConfig, inject, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { HttpInterceptorFn, provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { AuthService } from './core/services/auth.service';

const sessionInterceptor: HttpInterceptorFn = (request, next) => {
  if (!request.url.startsWith('/api/') || request.url.startsWith('/api/auth/')
      || request.headers.has('Authorization')) {
    return next(request);
  }

  const token = inject(AuthService).token();
  return next(token
    ? request.clone({ setHeaders: { Authorization: 'Bearer ' + token } })
    : request);
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([sessionInterceptor]))
  ]
};

