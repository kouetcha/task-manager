import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { Router } from '@angular/router';
import { AuthService } from '../services/AuthService';


export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.token;

  // 🔓 Pas de token → login / register
  if (!token) {
    return next(req);
  }

  // 🔐 Ajout du token
  const authReq = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });

  // 🔁 Gestion 401 (token expiré)
  return next(authReq).pipe(
    catchError(err => {
      if (err.status === 401) {
        authService.logout();
        router.navigate(['/login']);
      }
      return throwError(() => err);
    })
  );
};
