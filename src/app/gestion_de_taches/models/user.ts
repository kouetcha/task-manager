export class User {

  constructor(
    public id: number,
    public email: string,
    public telephone: string,
    public nom: string,
    public prenom: string,
    public motdepasse: string,
    public category: UserCategory=UserCategory.NORMAL,
    public profilePicture: string ,
    public dateInscription: Date,
    public profilePictureLink: string,
    public subscriptionPlan: string,
    public fullName: string,
    public roleProjet:string
  ) {
    fullName = `${prenom} ${nom}`;
  }

  
}
export enum UserCategory {
  ADMIN = 'ADMIN',
  NORMAL = 'NORMAL',
  SUPER_ADMIN='SUPER ADMIN'

}