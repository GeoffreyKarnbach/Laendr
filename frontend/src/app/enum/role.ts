export enum Role {
  ROLE_ADMIN = 'ROLE_ADMIN',
  ROLE_USER = 'ROLE_USER',
  ROLE_LENDER = 'ROLE_LENDER',
  ROLE_RENTER = 'ROLE_RENTER',
}

export const roleToDisplayText = {
  ROLE_ADMIN: 'Administrator',
  ROLE_USER: 'Authentifizierter Benutzer',
  ROLE_LENDER: 'Vermieter',
  ROLE_RENTER: 'Mieter',
};
