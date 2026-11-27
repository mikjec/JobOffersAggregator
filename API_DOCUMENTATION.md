# Dokumentacja API – Agregator Ofert Pracy

- **Base URL:** `http://localhost:8080`
- **Format danych:** `JSON` (`Content-Type: application/json`)
- **Uwierzytelnianie:** Bezstanowe (**JWT Bearer Token**) – nagłówek `Authorization: Bearer <token>`

---

## 1. Model ról i uprawnień

W systemie występują 3 role (`Role`) oraz dostęp niezalogowany:

| Rola / Stan | Przeglądanie (`GET` ofert i firm) | Tworzenie / Edycja / Usuwanie firm (`Company`) | Tworzenie / Edycja / Usuwanie ofert (`Offer`) | Zarządzanie użytkownikami (`/api/users/**`) |
|---|---|---|---|---|
| **Niezalogowany** | Tak | Nie (`403`) | Nie (`403`) | Nie (`403`) |
| **`USER`** | Tak | Nie (`403`) | Nie (`403`) | Nie (`403`) |
| **`EMPLOYER`** | Tak | Tak – **tylko własne firmy** | Tak – **tylko oferty własnych firm** | Nie (`403`) |
| **`ADMIN`** | Tak | Tak – **wszystkie** | Tak – **wszystkie** | Tak – **wszystkie** |

### Relacje własności
- **Firma (`Company`)** jest przypisana do użytkownika (`userId` / `user_id` w bazie).
  - Gdy `EMPLOYER` tworzy firmę, zostaje automatycznie przypisany jako jej właściciel.
  - `EMPLOYER` może edytować/usuwać wyłącznie firmy, których jest właścicielem (próba modyfikacji cudzej firmy zwraca `403 Forbidden`).
- **Oferta pracy (`Offer`)** musi być przypisana do firmy (`companyId` / `company_id` w bazie).
  - `EMPLOYER` może tworzyć, edytować i usuwać oferty tylko dla firm, których jest właścicielem.
- **`ADMIN`** omija ograniczenia własności i ma pełny dostęp do wszystkich zasobów.

---

## 2. Autoryzacja i Uwierzytelnianie (`/api/auth`)

Endpointy publiczne (`permitAll`). Hasła są hashowane algorytmem **BCrypt**.

### 2.1. Rejestracja użytkownika
- **Metoda i URL:** `POST /api/auth/register`
- **Body:**
```json
{
  "username": "jan_employer",
  "email": "jan@firma.pl",
  "password": "TajneHaslo123!",
  "role": "EMPLOYER"
}
```
*(Pole `role` przyjmuje wartości `USER`, `EMPLOYER`, `ADMIN`; jeśli pominięte, domyślnie ustawiane jest `USER`.)*
- **Odpowiedź `201 Created`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "jan_employer",
  "role": "EMPLOYER"
}
```
- **Odpowiedź `409 Conflict`:** gdy użytkownik o podanym `username` już istnieje.

### 2.2. Logowanie
- **Metoda i URL:** `POST /api/auth/login`
- **Body:**
```json
{
  "username": "jan_employer",
  "password": "TajneHaslo123!"
}
```
- **Odpowiedź `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "jan_employer",
  "role": "EMPLOYER"
}
```

---

## 3. Firmy (`/api/companies`)

### 3.1. Pobierz listę firm
- **Metoda i URL:** `GET /api/companies`
- **Dostęp:** Wszyscy (również niezalogowani)
- **Query Params:** `includeInactive` (opcjonalny, domyślnie `false`)
- **Odpowiedź `200 OK`:**
```json
[
  {
    "id": 1,
    "name": "TechCorp Sp. z o.o.",
    "industry": "Software Development",
    "userId": 1
  }
]
```

### 3.2. Pobierz firmę po ID
- **Metoda i URL:** `GET /api/companies/{id}`
- **Dostęp:** Wszyscy (również niezalogowani)
- **Odpowiedź:** `200 OK` / `404 Not Found`

### 3.3. Pobierz oferty przypisane do firmy
- **Metoda i URL:** `GET /api/companies/{id}/offers`
- **Dostęp:** Wszyscy (również niezalogowani)
- **Query Params:** `includeInactive` (opcjonalny, domyślnie `false`)
- **Odpowiedź:** `200 OK` (lista `OfferDTO`) / `404 Not Found`

### 3.4. Dodaj firmę
- **Metoda i URL:** `POST /api/companies`
- **Dostęp:** `EMPLOYER`, `ADMIN` (`Authorization: Bearer <token>`)
- **Body:**
```json
{
  "name": "TechCorp Sp. z o.o.",
  "industry": "Software Development"
}
```
*(Opcjonalnie `ADMIN` może przekazać `"userId": 2`, aby przypisać firmę do wskazanego użytkownika; dla `EMPLOYER` właściciel jest pobierany z tokena).*
- **Odpowiedź:** `201 Created`

### 3.5. Zaktualizuj firmę
- **Metoda i URL:** `PUT /api/companies/{id}`
- **Dostęp:** `EMPLOYER` (tylko własna firma), `ADMIN` (wszystkie)
- **Body:**
```json
{
  "name": "TechCorp Solutions",
  "industry": "IT Consulting"
}
```
- **Odpowiedź:** `200 OK` / `403 Forbidden` / `404 Not Found`

### 3.6. Soft Delete firmy
- **Metoda i URL:** `PATCH /api/companies/{id}/soft-delete` (lub `DELETE /api/companies/{id}/soft`)
- **Dostęp:** `EMPLOYER` (tylko własna firma), `ADMIN` (wszystkie)
- **Opis:** Ustawia `isActive = false`.
- **Odpowiedź:** `204 No Content` / `403 Forbidden` / `404 Not Found`

### 3.7. Hard Delete firmy
- **Metoda i URL:** `DELETE /api/companies/{id}` (lub `DELETE /api/companies/{id}/hard`)
- **Dostęp:** `EMPLOYER` (tylko własna firma), `ADMIN` (wszystkie)
- **Odpowiedź:** `204 No Content` / `403 Forbidden` / `404 Not Found`

---

## 4. Oferty pracy (`/api/offers`)

### 4.1. Pobierz listę ofert
- **Metoda i URL:** `GET /api/offers`
- **Dostęp:** Wszyscy (również niezalogowani)
- **Query Params:**
  - `includeInactive` (opcjonalny, domyślnie `false`)
  - `companyId` (opcjonalny – filtruje aktywne oferty danej firmy)
- **Odpowiedź `200 OK`:**
```json
[
  {
    "id": 1,
    "title": "Junior Java Developer",
    "location": "Warszawa / Remote",
    "companyId": 1
  }
]
```

### 4.2. Pobierz ofertę po ID
- **Metoda i URL:** `GET /api/offers/{id}`
- **Dostęp:** Wszyscy (również niezalogowani)
- **Odpowiedź:** `200 OK` / `404 Not Found`

### 4.3. Dodaj ofertę
- **Metoda i URL:** `POST /api/offers`
- **Dostęp:** `EMPLOYER` (tylko dla własnej firmy), `ADMIN` (dla dowolnej firmy)
- **Body:**
```json
{
  "title": "Junior Java Developer",
  "location": "Warszawa / Remote",
  "companyId": 1
}
```
- **Odpowiedź:** `201 Created` / `400 Bad Request` (brak `companyId`) / `403 Forbidden` (cudza firma) / `404 Not Found` (brak firmy)

### 4.4. Zaktualizuj ofertę
- **Metoda i URL:** `PUT /api/offers/{id}`
- **Dostęp:** `EMPLOYER` (tylko dla własnej firmy), `ADMIN` (wszystkie)
- **Body:**
```json
{
  "title": "Mid Java Developer",
  "location": "Lublin",
  "companyId": 1
}
```
- **Odpowiedź:** `200 OK` / `403 Forbidden` / `404 Not Found`

### 4.5. Soft Delete oferty
- **Metoda i URL:** `PATCH /api/offers/{id}/soft-delete` (lub `DELETE /api/offers/{id}/soft`)
- **Dostęp:** `EMPLOYER` (tylko dla własnej firmy), `ADMIN` (wszystkie)
- **Opis:** Ustawia `isActive = false`.
- **Odpowiedź:** `204 No Content` / `403 Forbidden` / `404 Not Found`

### 4.6. Hard Delete oferty
- **Metoda i URL:** `DELETE /api/offers/{id}` (lub `DELETE /api/offers/{id}/hard`)
- **Dostęp:** `EMPLOYER` (tylko dla własnej firmy), `ADMIN` (wszystkie)
- **Odpowiedź:** `204 No Content` / `403 Forbidden` / `404 Not Found`

---

## 5. Użytkownicy (`/api/users`)

Wszystkie operacje na `/api/users/**` są dostępne wyłącznie dla roli **`ADMIN`**.

- **`GET /api/users`** &rarr; lista `UserDTO` (`id`, `username`, `email`, `role` – bez hasła)
- **`GET /api/users/{id}`** &rarr; `200 OK` / `404 Not Found`
- **`POST /api/users`** &rarr; `201 Created`
- **`PUT /api/users/{id}`** &rarr; `200 OK` / `404 Not Found`
- **`DELETE /api/users/{id}`** &rarr; `204 No Content` / `404 Not Found`
